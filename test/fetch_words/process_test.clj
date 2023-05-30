(ns fetch-words.process-test
  (:require [clojure.test :refer :all]
            [fetch-words.process :refer :all]
            [clojure.java.io :as io]))

(def TEST_DATA_01 "'nie[tra][sound]первпервомerster.first<img src=\"xxx\" />'")
(def TEST_DATA_LINE_01 "INSERT INTO xxx VALUES(1,'DO',2,3,4,'','in erster Linie[ɪn ˈeːɐ̯stɐ ˈliːni̯ə][sound:xxx.mp3]на первом плане, в первую очередьНа первом плане&nbsp;вам нужно выздороветь.In erster Linie müssen Sie gesund werden.first and foremost<img src=\"xxx.jpg\" />','in erster Linie',5,6,'');")

(defn get_resource
  [name]
  (slurp (io/file (io/resource name))))

(deftest extract_payload_test
  (testing "extract payload data from a string with values"
    (let [text "1,'xx',2,3,4,'','ina,bc&nbsp;d.e.fg\"jpg\"','h',5,6,''"
          actual (extract_payload text)
          expected "ina,bc&nbsp;d.e.fg\"jpg\""]
      (is (= actual expected))))
  (testing "extract payload data from a string with values"
    (let [text (get_resource "one_line_values.txt")
          expected (get_resource "one_line_values_expected")
          actual (extract_payload text)]
      (is (= actual expected)))))

(deftest extract_data_items_from_sql_line_test
  (testing "empty line"
    (let [line ""
          actual (extract_data_items_from_sql_line line)
          expected ""]
      (is (= expected actual))))
  (testing "line without ending"
    (let [line "aa (bb,cc"
          actual (extract_data_items_from_sql_line line)
          expected "bb,cc"]
      (is (= expected actual))))
  (testing "line without beginning"
    (let [line "bb,cc);"
          actual (extract_data_items_from_sql_line line)
          expected "bb,cc"]
      (is (= expected actual))))
  (testing "normal line"
    (let [line "insert (bb, cc);"
          actual (extract_data_items_from_sql_line line)
          expected "bb, cc"]
      (is (= expected actual)))))

(deftest extract_words_from_one_item_test
  (testing "several words"
    (let [payload "one two three"
          actual (extract_words_from_one_item payload)
          expected {:words ["one" "two" "three"]
                    :orig "one two three"}]
      (is (= expected actual))))
  (testing "empty string"
    (let [payload ""
          actual (extract_words_from_one_item payload)
          expected {:words [""]
                    :orig ""}]
      (is (= expected actual)))))

(deftest process_lines_test
  (testing "one line"
    (let [lines [TEST_DATA_LINE_01]
          actual (process_lines lines)
          expected [#{"in" "erster" "Linie" "in erster Linie"}]]
      (is (= expected actual)))))
