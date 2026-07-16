(ns game-of-life.rle-test
  (:require [clojure.test :refer :all]
            [game-of-life.rle :as rle]
            [game-of-life.patterns :as patterns]
            [clojure.string :as str]))

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

;; ============================================================
;; Additional parse tests
;; ============================================================

(deftest parse-empty-rle-test
  (testing "parses empty RLE (just terminator)"
    (let [cells (rle/parse "!")]
      (is (empty? cells)))))

(deftest parse-single-live-cell-test
  (testing "parses single live cell"
    (let [cells (rle/parse "o!")]
      (is (= 1 (count cells)))
      (is (contains? cells [0 0])))))

(deftest parse-single-dead-cell-then-live-test
  (testing "parses dead cell followed by live cell"
    (let [cells (rle/parse "bo!")]
      (is (= 1 (count cells)))
      (is (contains? cells [1 0])))))

(deftest parse-large-run-count-test
  (testing "parses large run counts"
    (let [cells (rle/parse "x = 10, y = 1\n10o!")]
      (is (= 10 (count cells)))
      (is (contains? cells [0 0]))
      (is (contains? cells [9 0])))))

(deftest parse-mixed-dead-live-test
  (testing "parses alternating dead and live cells"
    ;; Pattern: .o.o.o  (3 live cells with gaps)
    (let [cells (rle/parse "bobobo!")]
      (is (= 3 (count cells)))
      (is (contains? cells [1 0]))
      (is (contains? cells [3 0]))
      (is (contains? cells [5 0])))))

(deftest parse-comments-only-test
  (testing "comments followed by data"
    (let [cells (rle/parse "#C comment\n#N name\nx = 3, y = 1\n3o!")]
      (is (= 3 (count cells))))))

(deftest parse-whitespace-in-data-test
  (testing "whitespace in data is skipped"
    (let [cells (rle/parse "x = 2, y = 2\n2o $\n2o!")]
      (is (= 4 (count cells))))))

(deftest parse-multiline-data-test
  (testing "data split across multiple lines"
    (let [cells (rle/parse "x = 3, y = 3\nbo$\n2bo$\n3o!")]
      ;; This is a glider
      (is (= 5 (count cells))))))

(deftest parse-double-digit-run-test
  (testing "double digit run count"
    (let [cells (rle/parse "x = 15, y = 1\n15o!")]
      (is (= 15 (count cells))))))

(deftest parse-with-trailing-content-after-bang-test
  (testing "content after ! terminator is ignored"
    (let [cells (rle/parse "3o!extra stuff here")]
      (is (= 3 (count cells))))))

;; ============================================================
;; Additional export tests
;; ============================================================

(deftest export-single-cell-test
  (testing "export single cell"
    (let [rle-str (rle/export #{[0 0]})]
      (is (str/includes? rle-str "x = 1, y = 1"))
      (is (str/includes? rle-str "o!")))))

(deftest export-contains-terminator-test
  (testing "exported RLE always ends with !"
    (let [rle-str (rle/export patterns/blinker)]
      (is (str/includes? rle-str "!")))))

(deftest export-glider-test
  (testing "export glider includes correct dimensions"
    (let [rle-str (rle/export patterns/glider)]
      (is (str/includes? rle-str "x = 3, y = 3")))))

;; ============================================================
;; Roundtrip tests for all patterns
;; ============================================================

(defn- normalize-cells
  "Normalize cells so minimum x and y are 0."
  [cells]
  (if (empty? cells)
    cells
    (let [min-x (reduce min (map first cells))
          min-y (reduce min (map second cells))]
      (set (map (fn [[x y]] [(- x min-x) (- y min-y)]) cells)))))

(deftest roundtrip-blinker-test
  (testing "blinker roundtrip"
    (let [original patterns/blinker
          parsed (rle/parse (rle/export original))]
      (is (= (normalize-cells original) parsed)))))

(deftest roundtrip-pulsar-test
  (testing "pulsar roundtrip"
    (let [original patterns/pulsar
          parsed (rle/parse (rle/export original))]
      (is (= (normalize-cells original) parsed)))))

(deftest roundtrip-lwss-test
  (testing "lightweight spaceship roundtrip"
    (let [original patterns/lightweight-spaceship
          parsed (rle/parse (rle/export original))]
      (is (= (normalize-cells original) parsed)))))

(deftest roundtrip-gosper-test
  (testing "gosper glider gun roundtrip"
    (let [original patterns/gosper-glider-gun
          parsed (rle/parse (rle/export original))]
      (is (= (normalize-cells original) parsed)))))

;; ============================================================
;; export-to-file / parse-file tests
;; ============================================================

(deftest export-to-file-test
  (testing "export-to-file creates a valid RLE file"
    (let [tmp-file (str (System/getProperty "java.io.tmpdir") "/gol-test-" (System/currentTimeMillis) ".rle")]
      (try
        (rle/export-to-file patterns/blinker tmp-file)
        (let [content (slurp tmp-file)]
          (is (str/includes? content "x = 3, y = 1"))
          (is (str/includes? content "!")))
        (finally
          (clojure.java.io/delete-file tmp-file true))))))

(deftest parse-file-test
  (testing "parse-file reads and parses an RLE file"
    (let [tmp-file (str (System/getProperty "java.io.tmpdir") "/gol-parse-test-" (System/currentTimeMillis) ".rle")]
      (try
        (spit tmp-file "x = 3, y = 1, rule = B3/S23\n3o!")
        (let [cells (rle/parse-file tmp-file)]
          (is (= 3 (count cells)))
          (is (contains? cells [0 0]))
          (is (contains? cells [1 0]))
          (is (contains? cells [2 0])))
        (finally
          (clojure.java.io/delete-file tmp-file true))))))

(deftest export-to-file-and-parse-file-roundtrip-test
  (testing "export-to-file then parse-file roundtrip"
    (let [tmp-file (str (System/getProperty "java.io.tmpdir") "/gol-roundtrip-" (System/currentTimeMillis) ".rle")]
      (try
        (rle/export-to-file patterns/glider tmp-file)
        (let [parsed (rle/parse-file tmp-file)]
          (is (= (normalize-cells patterns/glider) parsed)))
        (finally
          (clojure.java.io/delete-file tmp-file true))))))

;; ============================================================
;; Export run-length encoding tests (exercise run-counting loop)
;; ============================================================

(deftest export-run-count-2-test
  (testing "export emits a count prefix for runs of 2+ same-state cells"
    (let [cells #{[0 0] [1 0] [2 0] [3 0]}
          rle-str (rle/export cells)]
      (is (str/includes? rle-str "4o")))))

(deftest export-run-count-3-dead-then-alive-test
  (testing "export emits count for a run of dead cells followed by alive cells"
    (let [cells #{[3 0] [4 0] [5 0]}
          rle-str (rle/export cells)]
      ;; 3 dead, then 3 alive
      (is (str/includes? rle-str "3o")))))

(deftest export-single-row-no-trailing-dollar-test
  (testing "export of a single row has no trailing $"
    (let [rle-str (rle/export patterns/blinker)]
      ;; blinker is 1 row; no $ should appear before !
      (is (re-find #"3o!" rle-str)))))

(deftest export-multi-row-with-dead-gap-test
  (testing "export emits $ between rows and omits trailing dead cells"
    (let [cells #{[0 0] [0 2]}
          rle-str (rle/export cells)]
      ;; row 0: o, row 1: empty (just $), row 2: o
      (is (str/includes? rle-str "$")))))

(deftest parse-only-terminator-no-header-test
  (testing "parses just a bang with no header and no data"
    (let [cells (rle/parse "!")]
      (is (empty? cells)))))

(deftest parse-data-without-terminator-test
  (testing "parses data that ends without a bang (relies on empty chars)"
    (let [cells (rle/parse "3o")]
      (is (= 3 (count cells))))))
