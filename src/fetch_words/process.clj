(ns fetch-words.process
  (:require [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clj-http.client :as client]
            [clojure.tools.logging :refer [error]]))

(def BASE_URL_DWDS "https://www.dwds.de/wb/")
(def PAYLOAD_SEPARATOR_REGEX #"\x1f")
(def SEPARATOR \,)
(def QUOTE \')
(def WORD_INDEX 6)
(def SPACE_REGEX #"\s+")
(def BEGINNING_REGEX #"^[^(]+\(")
(def ENDING_REGEX #"\)[^)]+$")
(def BEGIN_BODY_BLOCK #"<[^<>]+\bclass=\"dwdswb-artikel\"[^<>]*>")
(def END_BODY_BLOCK #"<[^<>]+\bid=\"relation-block[^<>]+>")
(def BEGIN_AUDIO #"<audio>")
(def END_AUDIO #"</audio>")
(def AUDIO_TAG_REGEX #"<source\b[^<>]+\btype=\"audio\b[^<>]+>")
(def AUDIO_LINK_REGEX_BEGIN #"\bsrc=\"")
(def AUDIO_LINK_REGEX_END #"\">")
(def TABS_SEPARATOR_REGEX #"<div[^<>]+\bid=\"start-\d+\"[^<>]*>")
(def TABS_BEGIN_REGEX #"<main>")
(def TABS_END_REGEX #"</main>")

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

(defn extract_payload
  [data]
  (let [words (first (csv/read-csv data :separator SEPARATOR :quote QUOTE))]
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

(defn prepare_out_dir
  [word outdir levels]
  (throw (RuntimeException. "not implemented"))
  )

(defn fetch_word
  [word]
  (try
    (client/get (str BASE_URL_DWDS word))
    (catch Exception e
      (error e "exception for word:" word))))

(defn extract_by_begin_and_end_regex
  [text begin_regex end_regex]
  (let [[_ ending_text] (str/split text begin_regex)]
    (if (some? ending_text)
      (first (str/split ending_text end_regex)))))

(defn extract_body_part
  [body]
  (extract_by_begin_and_end_regex body BEGIN_BODY_BLOCK END_BODY_BLOCK))

(defn extract_audio_url
  [text]
  (let [audio_block (extract_by_begin_and_end_regex body BEGIN_AUDIO END_AUDIO)
        audio_source_tag (re-find AUDIO_TAG_REGEX audio_block)
        audio_url (extract_by_begin_and_end_regex
                   body
                   AUDIO_LINK_REGEX_BEGIN
                   AUDIO_LINK_REGEX_END)]
    audio_url))

(defn extract_audio_url_from_tab
  [text]
  (let [info_part (extract_body_part text)]
    (extract_audio_url info_part)))

(defn extract_audio_urls
  [tabs]
  (map extract_audio_url tabs))

(defn extract_separated_tabs
  [text]
  (let [tabs (str/split text TABS_SEPARATOR_REGEX)]
    (rest tabs)))

(defn extract_tabs_content
  [body]
  (let [all_tabs_content (extract_by_begin_and_end_regex
                          body
                          TABS_BEGIN_REGEX
                          TABS_END_REGEX)]
    (extract_separated_tabs all_tabs_content)))

(defn parse_multiple_tab_response
  [{body :body}]
  (let [tabs (extract_tabs_content body)]
    (extract_audio_urls tabs)))

(defn parse_single_tab_response
  [{body :body}]
  (extract_audio_url_from_tab body))

(defn single_tab?
  [response]
  (throw (RuntimeException. "not implemented"))
  )

(defn parse_response
  [response]
  (if (single_tab? response)
    [(parse_single_tab_response response)]
    (parse_multiple_tab_response response)))

(defn fetch_and_save_word
  [word full_out_dir]
  (throw (RuntimeException. "not implemented"))
  (let [
        response (fetch_word)
        data (parse_response response)
        ]
    )
)

(defn process_one_word
  [word outdir levels]
  (let [full_out_dir (prepare_out_dir word outdir levels)]
    (fetch_and_save_word word full_out_dir)))

(defn process_words
  [words outdir levels]
  (doseq [word words]
    (process_one_word word outdir levels)))

(defn process_word_list
  [wordlist outdir levels var_arguments]
  (let [words (read_word_list wordlist)]
    (process_words words outdir levels)))
