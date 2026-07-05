(ns game-of-life.patterns-test
  (:require [clojure.test :refer :all]
            [game-of-life.patterns :as patterns]))

(deftest pattern-counts-test
  (testing "glider has 5 cells"
    (is (= 5 (count patterns/glider))))
  (testing "blinker has 3 cells"
    (is (= 3 (count patterns/blinker))))
  (testing "pulsar has 48 cells"
    (is (= 48 (count patterns/pulsar))))
  (testing "gosper glider gun has 36 cells"
    (is (= 36 (count patterns/gosper-glider-gun))))
  (testing "lightweight spaceship has 9 cells"
    (is (= 9 (count patterns/lightweight-spaceship)))))

(deftest get-by-name-test
  (testing "finds patterns by name"
    (is (= patterns/glider (patterns/get-by-name "glider")))
    (is (= patterns/blinker (patterns/get-by-name "blinker")))
    (is (= patterns/gosper-glider-gun (patterns/get-by-name "gosper"))))
  (testing "case insensitive"
    (is (= patterns/glider (patterns/get-by-name "Glider")))
    (is (= patterns/glider (patterns/get-by-name "GLIDER"))))
  (testing "aliases work"
    (is (= patterns/gosper-glider-gun (patterns/get-by-name "gun")))
    (is (= patterns/gosper-glider-gun (patterns/get-by-name "gosperglidergun")))
    (is (= patterns/lightweight-spaceship (patterns/get-by-name "spaceship")))
    (is (= patterns/lightweight-spaceship (patterns/get-by-name "lwss"))))
  (testing "unknown returns nil"
    (is (nil? (patterns/get-by-name "nonexistent")))))

(deftest all-names-test
  (testing "all-names returns all pattern names"
    (let [names (set (patterns/all-names))]
      (is (contains? names "glider"))
      (is (contains? names "blinker"))
      (is (contains? names "pulsar"))
      (is (contains? names "gosper"))
      (is (contains? names "lwss"))))

  (testing "all-names count matches all-patterns"
    (is (= (count (patterns/all-names))
           (count patterns/all-patterns)))))

;; ============================================================
;; Additional pattern tests
;; ============================================================

(deftest all-patterns-map-test
  (testing "all-patterns map contains correct patterns"
    (is (= patterns/glider (get patterns/all-patterns "glider")))
    (is (= patterns/blinker (get patterns/all-patterns "blinker")))
    (is (= patterns/pulsar (get patterns/all-patterns "pulsar")))
    (is (= patterns/gosper-glider-gun (get patterns/all-patterns "gosper")))
    (is (= patterns/lightweight-spaceship (get patterns/all-patterns "lwss")))))

(deftest pattern-aliases-test
  (testing "all aliases resolve to valid pattern names"
    (doseq [[alias canonical] patterns/pattern-aliases]
      (is (contains? patterns/all-patterns canonical)
          (str "Alias " alias " -> " canonical " should be a valid pattern name")))))

(deftest get-by-name-alias-case-insensitive-test
  (testing "aliases are case-insensitive"
    (is (= patterns/gosper-glider-gun (patterns/get-by-name "GUN")))
    (is (= patterns/gosper-glider-gun (patterns/get-by-name "Gun")))
    (is (= patterns/lightweight-spaceship (patterns/get-by-name "SPACESHIP")))
    (is (= patterns/lightweight-spaceship (patterns/get-by-name "LightweightSpaceship")))))

(deftest patterns-are-sets-test
  (testing "all patterns are sets"
    (is (set? patterns/glider))
    (is (set? patterns/blinker))
    (is (set? patterns/pulsar))
    (is (set? patterns/gosper-glider-gun))
    (is (set? patterns/lightweight-spaceship))))

(deftest patterns-contain-valid-coords-test
  (testing "all pattern cells are [x y] vectors"
    (doseq [[name pattern] patterns/all-patterns]
      (doseq [cell pattern]
        (is (vector? cell) (str "Cell in " name " should be a vector"))
        (is (= 2 (count cell)) (str "Cell in " name " should have 2 elements"))
        (is (integer? (first cell)) (str "x in " name " should be integer"))
        (is (integer? (second cell)) (str "y in " name " should be integer"))))))
