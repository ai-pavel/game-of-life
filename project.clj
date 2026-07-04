(defproject game-of-life "0.1.0"
  :description "Conway's Game of Life in Clojure"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.1.230"]]
  :main game-of-life.core
  :aot [game-of-life.core]
  :profiles {:dev {:dependencies [[org.clojure/test.check "1.1.1"]]}})
