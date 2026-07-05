(ns game-of-life.renderer-test
  (:require [clojure.test :refer :all]
            [game-of-life.renderer :as renderer]
            [game-of-life.patterns :as patterns]
            [clojure.string :as str]))

;; The alive-char used by the renderer (U+2588 FULL BLOCK)
(def ^:private alive-char (char 0x2588))

;; ============================================================
;; render tests
;; ============================================================

(deftest render-empty-grid-test
  (testing "render empty grid produces all dead-char output"
    (let [result (renderer/render #{} {:width 3 :height 2 :offset-x 0 :offset-y 0})]
      ;; Should be 3 spaces, newline, 3 spaces
      (is (= "   \n   " result)))))

(deftest render-single-cell-test
  (testing "render single live cell at origin"
    (let [result (renderer/render #{[0 0]} {:width 3 :height 2 :offset-x 0 :offset-y 0})
          lines (str/split-lines result)]
      ;; First line should have alive char at position 0
      (is (= 2 (count lines)))
      (is (= alive-char (nth (first lines) 0))))))

(deftest render-with-offset-test
  (testing "render with offset shifts the viewport"
    (let [result (renderer/render #{[5 5]} {:width 3 :height 3 :offset-x 4 :offset-y 4})
          lines (str/split-lines result)]
      ;; Cell [5,5] should appear at viewport position [1,1]
      (is (= 3 (count lines)))
      (is (= alive-char (nth (nth lines 1) 1))))))

(deftest render-default-viewport-test
  (testing "render with default viewport options"
    (let [result (renderer/render #{[0 0]} {})]
      ;; Default is 80x24
      (let [lines (str/split-lines result)]
        (is (= 24 (count lines)))
        (is (= 80 (count (first lines))))))))

(deftest render-multiple-cells-test
  (testing "render shows multiple live cells"
    (let [result (renderer/render #{[0 0] [1 0] [2 0]} {:width 4 :height 2 :offset-x 0 :offset-y 0})
          lines (str/split-lines result)]
      ;; First line should have 3 alive chars followed by 1 dead
      (is (= 2 (count lines)))
      (is (= alive-char (nth (first lines) 0)))
      (is (= alive-char (nth (first lines) 1)))
      (is (= alive-char (nth (first lines) 2)))
      (is (= \space (nth (first lines) 3))))))

(deftest render-cell-outside-viewport-test
  (testing "cells outside viewport are not rendered"
    (let [result (renderer/render #{[100 100]} {:width 3 :height 3 :offset-x 0 :offset-y 0})]
      ;; All spaces - no alive chars
      (is (not (str/includes? result (str alive-char)))))))

(deftest render-1x1-viewport-test
  (testing "render with 1x1 viewport"
    (let [result-alive (renderer/render #{[0 0]} {:width 1 :height 1 :offset-x 0 :offset-y 0})
          result-dead (renderer/render #{} {:width 1 :height 1 :offset-x 0 :offset-y 0})]
      (is (= (str alive-char) result-alive))
      (is (= " " result-dead)))))

;; ============================================================
;; center-viewport tests
;; ============================================================

(deftest center-viewport-empty-test
  (testing "center-viewport with empty cells returns default"
    (let [vp (renderer/center-viewport #{} 80 24)]
      (is (= 80 (:width vp)))
      (is (= 24 (:height vp)))
      (is (= 0 (:offset-x vp)))
      (is (= 0 (:offset-y vp))))))

(deftest center-viewport-single-cell-test
  (testing "center-viewport centers on a single cell"
    (let [vp (renderer/center-viewport #{[10 10]} 80 24)]
      (is (= 80 (:width vp)))
      (is (= 24 (:height vp)))
      ;; Cell at [10,10], center should be offset-x = 10 - 40 = -30
      (is (= -30 (:offset-x vp)))
      (is (= -2 (:offset-y vp))))))

(deftest center-viewport-symmetric-pattern-test
  (testing "center-viewport with symmetric pattern"
    (let [cells #{[0 0] [4 0] [0 4] [4 4]}
          vp (renderer/center-viewport cells 10 10)]
      (is (= 10 (:width vp)))
      (is (= 10 (:height vp)))
      ;; center-x = (0+4)/2 = 2, offset-x = 2 - 5 = -3
      ;; center-y = (0+4)/2 = 2, offset-y = 2 - 5 = -3
      (is (= -3 (:offset-x vp)))
      (is (= -3 (:offset-y vp))))))

(deftest center-viewport-negative-coords-test
  (testing "center-viewport with negative coordinates"
    (let [cells #{[-10 -10] [10 10]}
          vp (renderer/center-viewport cells 20 20)]
      ;; center-x = (-10+10)/2 = 0, offset-x = 0 - 10 = -10
      ;; center-y = (-10+10)/2 = 0, offset-y = 0 - 10 = -10
      (is (= -10 (:offset-x vp)))
      (is (= -10 (:offset-y vp))))))

(deftest center-viewport-blinker-test
  (testing "center-viewport with blinker"
    (let [vp (renderer/center-viewport patterns/blinker 80 24)]
      (is (= 80 (:width vp)))
      (is (= 24 (:height vp)))
      ;; blinker is at [0,0] [1,0] [2,0], center is (1,0)
      (is (= (- 1 40) (:offset-x vp)))
      (is (= (- 0 12) (:offset-y vp))))))

;; ============================================================
;; render-to-console tests
;; ============================================================

(deftest render-to-console-test
  (testing "render-to-console outputs generation and population info"
    (let [output (with-out-str
                   (renderer/render-to-console #{[0 0] [1 0]}
                                               5
                                               {:width 3 :height 2 :offset-x 0 :offset-y 0}))]
      (is (str/includes? output "Generation: 5"))
      (is (str/includes? output "Population: 2")))))

(deftest render-to-console-escape-code-test
  (testing "render-to-console starts with cursor-home escape code"
    (let [output (with-out-str
                   (renderer/render-to-console #{} 0 {:width 1 :height 1 :offset-x 0 :offset-y 0}))]
      (is (str/includes? output "\033[H")))))

(deftest render-to-console-zero-population-test
  (testing "render-to-console with empty grid shows population 0"
    (let [output (with-out-str
                   (renderer/render-to-console #{} 0 {:width 1 :height 1 :offset-x 0 :offset-y 0}))]
      (is (str/includes? output "Population: 0")))))

;; ============================================================
;; animate tests
;; ============================================================

(deftest animate-zero-generations-test
  (testing "animate with 0 generations finishes immediately"
    (let [output (with-out-str
                   (renderer/animate #{[0 0]} :generations 0 :delay-ms 1 :width 3 :height 3))]
      ;; Should have the clear screen code but no generation output
      (is (str/includes? output "\033[2J")))))

(deftest animate-single-generation-test
  (testing "animate with 1 generation runs once"
    (let [output (with-out-str
                   (renderer/animate #{[0 0]} :generations 1 :delay-ms 1 :width 3 :height 3))]
      (is (str/includes? output "Generation: 0"))
      (is (str/includes? output "Population: 1")))))

(deftest animate-multiple-generations-test
  (testing "animate with a few generations runs correctly"
    (let [output (with-out-str
                   (renderer/animate patterns/blinker :generations 3 :delay-ms 1 :width 10 :height 5))]
      ;; Should contain generation info for gen 0, 1, 2
      (is (str/includes? output "Generation: 0"))
      (is (str/includes? output "Population: 3")))))

(deftest animate-empty-grid-test
  (testing "animate with empty grid"
    (let [output (with-out-str
                   (renderer/animate #{} :generations 2 :delay-ms 1 :width 5 :height 5))]
      (is (str/includes? output "Generation: 0"))
      (is (str/includes? output "Population: 0")))))
