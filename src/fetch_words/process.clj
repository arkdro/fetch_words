(ns fetch-words.process
  (:require [clojure.string :as str]
            [clojure.data.csv :as csv])
  )

(def SEPARATOR \;)
(def WORD_INDEX 6)

(defn split_words
  [phrase]
  (throw (RuntimeException. "not implemented"))
  )

(defn build_word_groups_of_one_phrase
  "Take a phrase, split in words, build a list containing
  the initial phrase, the main word if it's there, the all separated words"
  [phrase]
  (throw (RuntimeException. "not implemented"))
  (let [
        words (split_words phrase)
        ]
    )
  )

(defn build_word_groups
  [phrases]
  (map build_word_groups_of_one_phrase phrases))

(defn extract_data_items_from_sql_line
  [line]
  (let [without_beginning (str/replace-first line #"^[^(]+\(" "")
        without_ending (str/replace-first without_beginning #"\)[^)]+$" "")]
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

(defn extract_word_from_item
  [data_items]
  (throw (RuntimeException. "not implemented"))
  )

(defn extract_initial_phrases
  [text]
  (throw (RuntimeException. "not implemented"))
  )

(defn extract_words_from_items
  [data_items]
  (throw (RuntimeException. "not implemented"))
  )

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
