(ns fetch-words.process-test
  (:require [clojure.test :refer :all]
            [fetch-words.process :refer :all]
            [clojure.java.io :as io]
            ))

(def TEST_DATA_01 "'nie[tra][sound]первпервомerster.first<img src=\"xxx\" />'")

(defn get_resource
  [name]
  (slurp (io/file (io/resource name))))

(deftest extract_payload_test_simple
  (testing "extract payload data from a string with values"
    (let [text "1,'xx',2,3,4,'','ina,bc&nbsp;d.e.fg\"jpg\"','h',5,6,''"
          actual (extract_payload text)
          expected "ina,bc&nbsp;d.e.fg\"jpg\""]
      (is (= actual expected)))))

(deftest extract_payload_test_2
  (testing "extract payload data from a string with values"
    (let [text (get_resource "one_line_values.txt")
          expected (get_resource "one_line_values_expected")
          actual (extract_payload text)]
      (is (= actual expected)))))
