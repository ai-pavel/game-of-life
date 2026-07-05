(ns game-of-life.core-test
  (:require [clojure.test :refer :all]
            [game-of-life.core :as core]
            [game-of-life.patterns :as patterns]
            [clojure.string :as str]))

;; Access private functions via var
(def parse-int #'game-of-life.core/parse-int)
(def parse-opts #'game-of-life.core/parse-opts)

;; ============================================================
;; parse-int tests
;; ============================================================

(deftest parse-int-test
  (testing "parses valid integer strings"
    (is (= 42 (parse-int "42" 0)))
    (is (= 0 (parse-int "0" -1)))
    (is (= -5 (parse-int "-5" 0)))
    (is (= 100 (parse-int "100" 0))))

  (testing "returns default on invalid input"
    (is (= 99 (parse-int "abc" 99)))
    (is (= 0 (parse-int "" 0)))
    (is (= -1 (parse-int "12.5" -1)))
    (is (= 50 (parse-int nil 50))))

  (testing "returns default for non-numeric strings"
    (is (= 10 (parse-int "hello" 10)))
    (is (= 7 (parse-int "0x1F" 7)))))

;; ============================================================
;; parse-opts tests
;; ============================================================

(deftest parse-opts-test
  (let [option-defs {"--delay" {:key :delay}
                     "-d"      {:key :delay}
                     "--generations" {:key :generations}
                     "-g"           {:key :generations}}]
    (testing "parses known options"
      (is (= {:delay "200"} (parse-opts ["--delay" "200"] option-defs)))
      (is (= {:delay "50"} (parse-opts ["-d" "50"] option-defs)))
      (is (= {:generations "100"} (parse-opts ["--generations" "100"] option-defs)))
      (is (= {:generations "10"} (parse-opts ["-g" "10"] option-defs))))

    (testing "parses multiple options"
      (is (= {:delay "200" :generations "50"}
             (parse-opts ["--delay" "200" "--generations" "50"] option-defs))))

    (testing "empty args returns empty map"
      (is (= {} (parse-opts [] option-defs))))

    (testing "unknown options are skipped"
      (is (= {:delay "100"}
             (parse-opts ["--unknown" "val" "--delay" "100"] option-defs))))

    (testing "option at end without value returns current result"
      (is (= {} (parse-opts ["--delay"] option-defs))))

    (testing "last value wins for same key"
      (is (= {:delay "300"}
             (parse-opts ["--delay" "200" "-d" "300"] option-defs))))))

;; ============================================================
;; run-command tests
;; ============================================================

(deftest run-command-unknown-pattern-test
  (testing "run-command with unknown pattern prints error to stderr"
    (let [err-output (with-out-str
                       (binding [*err* *out*]
                         (core/run-command ["nonexistent"])))]
      (is (str/includes? err-output "Unknown pattern: nonexistent"))
      (is (str/includes? err-output "Available patterns:")))))

;; ============================================================
;; load-command tests
;; ============================================================

(deftest load-command-no-args-test
  (testing "load-command with no args prints usage to stderr"
    (let [err-output (with-out-str
                       (binding [*err* *out*]
                         (core/load-command [])))]
      (is (str/includes? err-output "Usage: load <file.rle>")))))

(deftest load-command-missing-file-test
  (testing "load-command with non-existent file prints error"
    (let [err-output (with-out-str
                       (binding [*err* *out*]
                         (core/load-command ["/no/such/file.rle"])))]
      (is (str/includes? err-output "File not found:")))))

;; ============================================================
;; export-command tests
;; ============================================================

(deftest export-command-unknown-pattern-test
  (testing "export-command with unknown pattern prints error to stderr"
    (let [err-output (with-out-str
                       (binding [*err* *out*]
                         (core/export-command ["nonexistent"])))]
      (is (str/includes? err-output "Unknown pattern: nonexistent")))))

(deftest export-command-to-stdout-test
  (testing "export-command prints RLE to stdout for known pattern"
    (let [output (with-out-str (core/export-command ["blinker"]))]
      (is (str/includes? output "x = 3, y = 1"))
      (is (str/includes? output "rule = B3/S23")))))

(deftest export-command-to-file-test
  (testing "export-command writes RLE to file with --output"
    (let [tmp-file (str (System/getProperty "java.io.tmpdir") "/test-export-" (System/currentTimeMillis) ".rle")
          output (with-out-str (core/export-command ["blinker" "--output" tmp-file]))
          file-content (slurp tmp-file)]
      (is (str/includes? output "Exported blinker to"))
      (is (str/includes? file-content "x = 3, y = 1"))
      (clojure.java.io/delete-file tmp-file true))))

(deftest export-command-to-file-short-flag-test
  (testing "export-command writes RLE to file with -o"
    (let [tmp-file (str (System/getProperty "java.io.tmpdir") "/test-export-o-" (System/currentTimeMillis) ".rle")
          output (with-out-str (core/export-command ["glider" "-o" tmp-file]))
          file-content (slurp tmp-file)]
      (is (str/includes? output "Exported glider to"))
      (is (str/includes? file-content "rule = B3/S23"))
      (clojure.java.io/delete-file tmp-file true))))

;; ============================================================
;; step-command tests
;; ============================================================

(deftest step-command-known-pattern-test
  (testing "step-command prints generation info for known pattern"
    (let [output (with-out-str (core/step-command ["blinker"]))]
      (is (str/includes? output "Initial: Generation 0, Population 3"))
      (is (str/includes? output "After 1 step(s): Generation 1, Population 3")))))

(deftest step-command-multiple-steps-test
  (testing "step-command with --steps flag"
    (let [output (with-out-str (core/step-command ["blinker" "--steps" "4"]))]
      (is (str/includes? output "After 4 step(s): Generation 4, Population 3")))))

(deftest step-command-short-flag-test
  (testing "step-command with -n flag"
    (let [output (with-out-str (core/step-command ["blinker" "-n" "2"]))]
      (is (str/includes? output "After 2 step(s): Generation 2, Population 3")))))

(deftest step-command-unknown-pattern-test
  (testing "step-command with unknown pattern prints error to stderr"
    (let [err-output (with-out-str
                       (binding [*err* *out*]
                         (core/step-command ["nonexistent"])))]
      (is (str/includes? err-output "Unknown pattern: nonexistent")))))

(deftest step-command-default-pattern-test
  (testing "step-command defaults to glider when no pattern given"
    (let [output (with-out-str (core/step-command []))]
      (is (str/includes? output "Initial: Generation 0, Population 5")))))

;; ============================================================
;; list-patterns tests
;; ============================================================

(deftest list-patterns-test
  (testing "list-patterns prints all pattern names"
    (let [output (with-out-str (core/list-patterns))]
      (is (str/includes? output "Available patterns:"))
      (is (str/includes? output "glider"))
      (is (str/includes? output "blinker"))
      (is (str/includes? output "pulsar"))
      (is (str/includes? output "gosper"))
      (is (str/includes? output "lwss")))))

;; ============================================================
;; print-usage tests
;; ============================================================

(deftest print-usage-test
  (testing "print-usage shows usage info"
    (let [output (with-out-str (core/print-usage))]
      (is (str/includes? output "Conway's Game of Life"))
      (is (str/includes? output "Usage:"))
      (is (str/includes? output "lein run run"))
      (is (str/includes? output "lein run load"))
      (is (str/includes? output "lein run export"))
      (is (str/includes? output "lein run step"))
      (is (str/includes? output "lein run patterns")))))

;; ============================================================
;; -main tests
;; ============================================================

(deftest main-no-args-test
  (testing "-main with no args prints usage"
    (let [output (with-out-str (core/-main))]
      (is (str/includes? output "Usage:")))))

(deftest main-help-test
  (testing "-main with help flag prints usage"
    (let [output (with-out-str (core/-main "help"))]
      (is (str/includes? output "Usage:")))
    (let [output (with-out-str (core/-main "--help"))]
      (is (str/includes? output "Usage:")))
    (let [output (with-out-str (core/-main "-h"))]
      (is (str/includes? output "Usage:")))))

(deftest main-patterns-test
  (testing "-main with 'patterns' lists patterns"
    (let [output (with-out-str (core/-main "patterns"))]
      (is (str/includes? output "Available patterns:"))
      (is (str/includes? output "glider")))))

(deftest main-step-test
  (testing "-main with 'step' runs step-command"
    (let [output (with-out-str (core/-main "step" "blinker"))]
      (is (str/includes? output "Initial: Generation 0")))))

(deftest main-export-test
  (testing "-main with 'export' runs export-command"
    (let [output (with-out-str (core/-main "export" "blinker"))]
      (is (str/includes? output "x = 3, y = 1")))))

(deftest main-unknown-command-test
  (testing "-main with unknown command prints error and usage"
    (let [output (with-out-str
                   (binding [*err* *out*]
                     (core/-main "bogus")))]
      (is (str/includes? output "Unknown command: bogus")))))

(deftest main-case-insensitive-test
  (testing "-main command is case-insensitive"
    (let [output (with-out-str (core/-main "PATTERNS"))]
      (is (str/includes? output "Available patterns:")))
    (let [output (with-out-str (core/-main "Step" "blinker"))]
      (is (str/includes? output "Initial: Generation 0")))))

(deftest main-load-no-args-test
  (testing "-main load with no file args prints usage"
    (let [err-output (with-out-str
                       (binding [*err* *out*]
                         (core/-main "load")))]
      (is (str/includes? err-output "Usage: load <file.rle>")))))

(deftest main-run-unknown-pattern-test
  (testing "-main run with unknown pattern prints error"
    (let [err-output (with-out-str
                       (binding [*err* *out*]
                         (core/-main "run" "nonexistent")))]
      (is (str/includes? err-output "Unknown pattern: nonexistent")))))
