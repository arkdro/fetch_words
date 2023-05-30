(ns fetch-words.process
  (:require [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clj-http.client :as client]
            [clojure.tools.logging :refer [error warn]]))

(def BASE_URL_DWDS "https://www.dwds.de/wb/")
(def PAYLOAD_SEPARATOR_REGEX #"\x1f")
(def SEPARATOR \,)
(def QUOTE \')
(def WORD_INDEX 6)
(def SPACE_REGEX #"\s+")
(def BEGINNING_REGEX #"^[^(]+\(")
(def ENDING_REGEX #"\)[^)]+$")
(def BEGIN_BODY_BLOCK #"(?i)<[^<>]+\bclass=\"dwdswb-artikel\"[^<>]*>")
(def END_BODY_BLOCK #"(?i)<[^<>]+\bid=\"relation-block[^<>]+>")
(def BEGIN_AUDIO #"(?i)<audio>")
(def END_AUDIO #"(?i)</audio>")
(def AUDIO_TAG_REGEX #"(?i)<source\b[^<>]+\btype=\"audio\b[^<>]+>")
(def AUDIO_LINK_REGEX #"(?i)\bSRC=\"([^<>\"]+)\"")
(def TABS_REGEX #"(?i)\bid=\"dwdswb-tabs\"")
(def TABS_SEPARATOR_REGEX #"(?i)<div[^<>]+\bid=\"start-\d+\"[^<>]*>")
(def TABS_BEGIN_REGEX #"(?i)<main>")
(def TABS_END_REGEX #"(?i)</main>")
(def ONE_LEVEL_CHAR_REGEX  #"(..?)(.*)") ;; take 2 chars if possible and take the rest

(defn build_word_groups_of_one_phrase
  "Currently no word groups. Only the original words and the whole sentence."
  [{:keys [words orig]}]
  (conj
   (into #{} words)
   orig))

(defn build_word_groups
  [phrases]
  (map build_word_groups_of_one_phrase phrases))

(defn extract_data_items_from_sql_line
  [line]
  (let [without_beginning (str/replace-first line BEGINNING_REGEX "")
        without_ending (str/replace-first without_beginning ENDING_REGEX "")]
    without_ending))

(defn extract_values_from_insertion_lines
  [lines]
  (map extract_data_items_from_sql_line lines))

(defn split_csv_string
  [data]
  (csv/read-csv data :separator SEPARATOR :quote QUOTE))

(defn extract_payload
  [data]
  (let [words (first (split_csv_string data))]
    (get words WORD_INDEX)))

(defn extract_payload_items
  [data_items]
  (map extract_payload data_items))

(defn extract_useful_payload
  [payload]
  (let [parts (str/split payload PAYLOAD_SEPARATOR_REGEX)]
    (first parts)))

(defn extract_useful_payload_items
  [payload_items]
  (map extract_useful_payload payload_items))

(defn extract_words_from_one_item
  [data_item]
  (let [words (str/split data_item SPACE_REGEX)]
    {:words words
     :orig data_item}))

(defn extract_words_from_payload_items
  [payload_items]
  (map extract_words_from_one_item payload_items))

(defn get_lines
  [filename]
  (->> filename
       slurp
       str/split-lines))

(defn process_lines
  [lines]
  (let [data_parts (extract_values_from_insertion_lines lines)
        payload_items (extract_payload_items data_parts)
        useful_payload_items (extract_useful_payload_items payload_items)
        words (extract_words_from_payload_items useful_payload_items)
        word_groups (build_word_groups words)]
    word_groups))

(defn read_word_list
  [wordlist]
  (let [lines (get_lines wordlist)
        word_groups (process_lines lines)]
    word_groups))

(defn split_word_into_parts
  "Split a word into parts using a pre-defined regex.
  The total number of parts in the result is less or equal to 'levels'."
  ([word max_parts]
   (split_word_into_parts [] word (- max_parts 2)))
  ([acc word max_parts]
   (cond
     (str/blank? word) acc
     (neg? max_parts) (conj acc word)
     :default (let [[_ part end] (re-find ONE_LEVEL_CHAR_REGEX word)
                    new_acc (conj acc part)]
                (recur new_acc end (dec max_parts))))))

(defn create_out_dir
  [dir]
  (let [attributes (into-array java.nio.file.attribute.FileAttribute [])]
    (java.nio.file.Files/createDirectories dir attributes)))

(defn build_out_dir_name
  "Split a word into parts, join the base dir and the parts
  to build a deep nested directory name."
  [word outdir levels]
  (let [parts (split_word_into_parts word levels)
        var_arg_parts (into-array java.lang.String parts)]
    (java.nio.file.Paths/get outdir var_arg_parts)))

(defn prepare_out_dir
  [word outdir levels]
  (let [dir (build_out_dir_name word outdir levels)]
    dir))

(defn fetch_word
  [word]
  (try
    (client/get (str BASE_URL_DWDS word))
    (catch Exception e
      (error e "exception for word:" word))))

(defn extract_by_begin_and_end_regex
  [begin_regex end_regex text]
  (let [[_ ending_text] (str/split text begin_regex)]
    (if (some? ending_text)
      (first (str/split ending_text end_regex)))))

(defn extract_body_part
  [body]
  (extract_by_begin_and_end_regex BEGIN_BODY_BLOCK END_BODY_BLOCK body))

(defn extract_audio_url
  [text]
  (some->> text
           (extract_by_begin_and_end_regex BEGIN_AUDIO END_AUDIO)
           (re-find AUDIO_TAG_REGEX)
           (re-find AUDIO_LINK_REGEX)
           (second)))

(defn extract_audio_url_from_tab
  [text]
  (let [info_part (extract_body_part text)]
    (extract_audio_url info_part)))

(defn extract_audio_urls
  [tabs]
  (map extract_audio_url tabs))

(defn extract_separated_tabs
  [text]
  (some-> text
          (str/split TABS_SEPARATOR_REGEX)
          (rest)))

(defn extract_tabs_content
  [body]
  (let [all_tabs_content (extract_by_begin_and_end_regex
                          TABS_BEGIN_REGEX
                          TABS_END_REGEX
                          body)]
    (extract_separated_tabs all_tabs_content)))

(defn parse_multiple_tab_response
  [body]
  (let [tabs (extract_tabs_content body)]
    (extract_audio_urls tabs)))

(defn parse_single_tab_response
  [body]
  (extract_audio_url_from_tab body))

(defn multiple_tabs?
  [body]
  (re-find TABS_REGEX body))

(defn parse_response
  [{body :body}]
  (if (multiple_tabs? body)
    (parse_multiple_tab_response body)
    [(parse_single_tab_response body)]))

(defn fetch_url
  [url]
  (let [{body :body status :status} (client/get url)]
    (if (= status 200)
      body
      (warn "no data for url:" url ", status:" status))))

(defn build_directory_and_file_name
  [word directory]
  (.getPath (java.io.File.
             (str directory)
             word)))

(defn build_full_filename
  [url directory]
  (let [server_path (.getPath (java.net.URL. url))
        filename (.getName (java.io.File. server_path))]
    (build_directory_and_file_name filename directory)))

(defn create_directory_and_save_file
  [dir filename data]
  (create_out_dir dir)
  (spit filename data))

(defn fetch_and_save_url
  [url directory]
  (let [filename (build_full_filename url directory)
        data (fetch_url url)]
    (if (some? data)
      (create_directory_and_save_file directory filename data))))

(defn remove_nil_urls
  [urls]
  (filter some? urls))

(defn fetch_and_save_word
  [word directory]
  (let [response (fetch_word word)
        urls (parse_response response)
        non_nil_urls (remove_nil_urls urls)]
    (doseq
        [url non_nil_urls]
      (fetch_and_save_url url directory))))

(defn process_one_word
  [word outdir levels]
  (let [full_out_dir (prepare_out_dir word outdir levels)]
    (fetch_and_save_word word full_out_dir)))

(defn do_non_zero_random_delay
  [delay_min delay_max]
  (let [delta (- delay_max delay_min)
        random_value (rand-int delta)
        delay (+ delay_min random_value)]
    (Thread/sleep delay)))

(defn do_random_delay
  [delay_min delay_max]
  (if (> delay_max 0)
    (do_non_zero_random_delay delay_min delay_max)))

(defn process_one_word_with_delay
  [word outdir levels delay_min delay_max]
  (do_random_delay delay_min delay_max)
  (process_one_word word outdir levels))

(defn process_one_word_set
  "Take a set of words related to the main word,
  process each word separately."
  [word_set outdir levels delay_min delay_max]
  (map
   #(process_one_word_with_delay % outdir levels delay_min delay_max)
   word_set))

(defn process_words
  [words outdir levels delay_min delay_max]
  (doseq [word_set words]
    (process_one_word_set word_set outdir levels delay_min delay_max)))

(defn process_word_list
  [wordlist outdir levels delay_min delay_max var_arguments]
  (let [words (read_word_list wordlist)]
    (process_words words outdir levels delay_min delay_max)))
