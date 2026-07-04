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
      (is (contains? result [12 20])))))
