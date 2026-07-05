(ns game-of-life.grid-test
  (:require [clojure.test :refer :all]
            [game-of-life.grid :as grid]
            [game-of-life.patterns :as patterns]))

(deftest neighbors-test
  (testing "returns exactly 8 neighbors"
    (is (= 8 (count (grid/neighbors [0 0])))))
  (testing "does not include the cell itself"
    (is (not (some #{[0 0]} (grid/neighbors [0 0]))))))

(deftest step-test
  (testing "empty grid stays empty"
    (is (empty? (grid/step #{}))))

  (testing "lone cell dies"
    (is (empty? (grid/step #{[5 5]}))))

  (testing "block is a still life"
    (let [block #{[0 0] [1 0] [0 1] [1 1]}]
      (is (= block (grid/step block)))))

  (testing "blinker oscillates with period 2"
    (let [phase1 patterns/blinker
          phase2 (grid/step phase1)
          phase3 (grid/step phase2)]
      ;; Phase 2 should be vertical
      (is (contains? phase2 [1 -1]))
      (is (contains? phase2 [1 0]))
      (is (contains? phase2 [1 1]))
      (is (= 3 (count phase2)))
      ;; Phase 3 should be back to horizontal
      (is (= phase1 phase3)))))

(deftest step-n-test
  (testing "step-n 0 returns original"
    (is (= patterns/blinker (grid/step-n patterns/blinker 0))))

  (testing "step-n 10 with blinker preserves population"
    (is (= 3 (count (grid/step-n patterns/blinker 10))))))

(deftest glider-moves-test
  (testing "glider moves (1,1) after 4 steps"
    (let [result (grid/step-n patterns/glider 4)]
      (is (= 5 (count result)))
      (is (contains? result [2 1]))
      (is (contains? result [3 2]))
      (is (contains? result [1 3]))
      (is (contains? result [2 3]))
      (is (contains? result [3 3])))))

(deftest bounds-test
  (testing "empty grid returns nil"
    (is (nil? (grid/bounds #{}))))

  (testing "returns correct bounds"
    (let [cells #{[-5 3] [10 -2]}
          [min-x min-y max-x max-y] (grid/bounds cells)]
      (is (= -5 min-x))
      (is (= -2 min-y))
      (is (= 10 max-x))
      (is (= 3 max-y)))))

(deftest add-pattern-test
  (testing "add-pattern with offset"
    (let [result (grid/add-pattern #{} patterns/blinker 10 20)]
      (is (contains? result [10 20]))
      (is (contains? result [11 20]))
      (is (contains? result [12 20]))))

  (testing "add-pattern with default offset (2-arity)"
    (let [result (grid/add-pattern #{} patterns/blinker)]
      (is (= patterns/blinker result))))

  (testing "add-pattern merges with existing cells"
    (let [existing #{[5 5]}
          result (grid/add-pattern existing patterns/blinker 10 10)]
      (is (contains? result [5 5]))
      (is (contains? result [10 10]))
      (is (contains? result [11 10]))
      (is (contains? result [12 10]))
      (is (= 4 (count result)))))

  (testing "add-pattern with negative offset"
    (let [result (grid/add-pattern #{} #{[0 0]} -5 -10)]
      (is (contains? result [-5 -10]))))

  (testing "add-pattern with empty pattern"
    (let [result (grid/add-pattern #{[1 1]} #{} 0 0)]
      (is (= #{[1 1]} result)))))

;; ============================================================
;; Additional edge case tests
;; ============================================================

(deftest neighbors-specific-coords-test
  (testing "neighbors of non-origin cell"
    (let [n (set (grid/neighbors [5 5]))]
      (is (= 8 (count n)))
      (is (contains? n [4 4]))
      (is (contains? n [5 4]))
      (is (contains? n [6 4]))
      (is (contains? n [4 5]))
      (is (contains? n [6 5]))
      (is (contains? n [4 6]))
      (is (contains? n [5 6]))
      (is (contains? n [6 6]))))

  (testing "neighbors of negative coordinate cell"
    (let [n (set (grid/neighbors [-1 -1]))]
      (is (= 8 (count n)))
      (is (contains? n [-2 -2]))
      (is (contains? n [0 0])))))

(deftest step-two-cells-test
  (testing "two adjacent cells both die"
    (is (empty? (grid/step #{[0 0] [1 0]})))))

(deftest step-three-cells-inline-test
  (testing "three inline cells form a blinker"
    (let [cells #{[0 0] [1 0] [2 0]}
          next-gen (grid/step cells)]
      (is (= 3 (count next-gen)))
      (is (contains? next-gen [1 -1]))
      (is (contains? next-gen [1 0]))
      (is (contains? next-gen [1 1])))))

(deftest step-preserves-set-type-test
  (testing "step returns a set"
    (is (set? (grid/step #{})))
    (is (set? (grid/step #{[0 0] [1 0] [2 0]})))))

(deftest step-n-one-step-test
  (testing "step-n with 1 equals step"
    (let [cells patterns/blinker]
      (is (= (grid/step cells) (grid/step-n cells 1))))))

(deftest step-n-large-number-test
  (testing "step-n with even number on blinker returns to original"
    (is (= patterns/blinker (grid/step-n patterns/blinker 100)))))

(deftest bounds-single-cell-test
  (testing "bounds of single cell"
    (let [[min-x min-y max-x max-y] (grid/bounds #{[3 7]})]
      (is (= 3 min-x))
      (is (= 7 min-y))
      (is (= 3 max-x))
      (is (= 7 max-y)))))

(deftest bounds-negative-coords-test
  (testing "bounds with negative coordinates"
    (let [[min-x min-y max-x max-y] (grid/bounds #{[-5 -3] [5 3]})]
      (is (= -5 min-x))
      (is (= -3 min-y))
      (is (= 5 max-x))
      (is (= 3 max-y)))))

(deftest bounds-blinker-test
  (testing "bounds of blinker pattern"
    (let [[min-x min-y max-x max-y] (grid/bounds patterns/blinker)]
      (is (= 0 min-x))
      (is (= 0 min-y))
      (is (= 2 max-x))
      (is (= 0 max-y)))))
