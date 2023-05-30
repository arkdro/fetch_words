(ns fetch-words.process
  (:require [clojure.string :as str]
            [clojure.data.csv :as csv])
  )

(def SEPARATOR \,)
(def QUOTE \')
(def WORD_INDEX 6)
(def SPACE_REGEX #"\s+")
(def BEGINNING_REGEX #"^[^(]+\(")
(def ENDING_REGEX #"\)[^)]+$")

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
        words (extract_words_from_payload_items payload_items)
        word_groups (build_word_groups words)]
    word_groups))

(defn read_word_list
  [wordlist]
  (let [lines (get_lines wordlist)
        word_groups (process_lines lines)]
    word_groups))

(defn prepare_out_dir
  [word outdir levels]
  )

(defn fetch_and_save_word
  [word full_out_dir]
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
