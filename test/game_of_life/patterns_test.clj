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
      (is (contains? names "lwss")))))
