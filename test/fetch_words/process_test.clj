(ns fetch-words.process-test
  (:require [clojure.test :refer :all]
            [fetch-words.process :refer :all]
            [clojure.java.io :as io]))

(def TEST_DATA_01 "'nie[tra][sound]первпервомerster.first<img src=\"xxx\" />'")

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
