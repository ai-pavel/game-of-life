(ns game-of-life.rle-test
  (:require [clojure.test :refer :all]
            [game-of-life.rle :as rle]
            [game-of-life.patterns :as patterns]))

(deftest parse-glider-test
  (testing "parses glider RLE"
    (let [rle-str "#N Glider\n#C A small spaceship\nx = 3, y = 3, rule = B3/S23\nbo$2bo$3o!"
          cells (rle/parse rle-str)]
      (is (= 5 (count cells)))
      (is (contains? cells [1 0]))
      (is (contains? cells [2 1]))
      (is (contains? cells [0 2]))
      (is (contains? cells [1 2]))
      (is (contains? cells [2 2])))))

(deftest parse-blinker-test
  (testing "parses blinker RLE"
    (let [cells (rle/parse "x = 3, y = 1, rule = B3/S23\n3o!")]
      (is (= 3 (count cells)))
      (is (contains? cells [0 0]))
      (is (contains? cells [1 0]))
      (is (contains? cells [2 0])))))

(deftest parse-run-counts-test
  (testing "parses run counts correctly"
    (let [cells (rle/parse "x = 5, y = 1\n5o!")]
      (is (= 5 (count cells))))))

(deftest parse-multiple-rows-test
  (testing "parses 2x2 block"
    (let [cells (rle/parse "x = 2, y = 2\n2o$2o!")]
      (is (= 4 (count cells)))
      (is (contains? cells [0 0]))
      (is (contains? cells [1 0]))
      (is (contains? cells [0 1]))
      (is (contains? cells [1 1])))))

(deftest parse-skips-rows-test
  (testing "handles multiple row skips"
    (let [cells (rle/parse "x = 1, y = 4\no3$o!")]
      (is (= 2 (count cells)))
      (is (contains? cells [0 0]))
      (is (contains? cells [0 3])))))

(deftest parse-no-header-test
  (testing "parses RLE without header"
    (let [cells (rle/parse "3o!")]
      (is (= 3 (count cells))))))

(deftest export-empty-test
  (testing "exports empty cells to valid RLE"
    (let [rle-str (rle/export #{})]
      (is (clojure.string/includes? rle-str "x = 0, y = 0"))
      (is (clojure.string/includes? rle-str "!")))))

(deftest export-header-test
  (testing "export contains correct header"
    (let [rle-str (rle/export patterns/blinker)]
      (is (clojure.string/includes? rle-str "x = 3, y = 1"))
      (is (clojure.string/includes? rle-str "rule = B3/S23")))))

(deftest roundtrip-test
  (testing "export and re-parse produces equivalent cells"
    (let [original patterns/glider
          rle-str (rle/export original)
          parsed (rle/parse rle-str)
          ;; Normalize: the export shifts to min corner = (0,0)
          min-x (reduce min (map first original))
          min-y (reduce min (map second original))
          normalized (set (map (fn [[x y]] [(- x min-x) (- y min-y)]) original))]
      (is (= (count normalized) (count parsed)))
      (is (= normalized parsed)))))
