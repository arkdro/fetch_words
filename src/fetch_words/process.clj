(ns fetch-words.process
  (:require [clojure.string :as str]
            [clojure.data.csv :as csv])
  )

(def SEPARATOR \,)
(def WORD_INDEX 6)
(def SPACE_REGEX #"\s+")
(def BEGINNING_REGEX #"^[^(]+\(")
(def ENDING_REGEX #"\)[^)]+$")

(defn build_word_groups_of_one_phrase
  "Take a list of words, build a list containing
  the initial phrase, the main word if it's there and all words separated"
  [words]
  (throw (RuntimeException. "not implemented"))
  (let [
        ]
    )
  )

(defn build_word_groups
  [phrases]
  (map build_word_groups_of_one_phrase phrases))

(defn extract_data_items_from_sql_line
  [line]
  (let [without_beginning (str/replace-first line BEGINNING_REGEX "")
        without_ending (str/replace-first without_beginning ENDING_REGEX "")]
    without_ending))

(defn extract_data_from_whole_content
  [lines]
  (map extract_data_items_from_sql_line lines))

(defn extract_one_data_item
  [data]
  (let [words (first (csv/read-csv data :separator SEPARATOR))]
    (get words WORD_INDEX)))

(defn extract_data_items
  [data_items]
  (map extract_one_data_item data_items))

(defn extract_words_from_one_item
  [data_item]
  (str/split data_item SPACE_REGEX))

(defn extract_words_from_items
  [data_items]
  (map extract_words_from_one_item data_items))

(defn read_word_list
  [wordlist]
  (let [
        whole_content (slurp wordlist)
        data_parts (extract_data_from_whole_content whole_content)
        data_items (extract_data_items data_parts)
        words (extract_words_from_items data_items)
        word_groups (build_word_groups words)
        ]
    word_groups)
  )

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
