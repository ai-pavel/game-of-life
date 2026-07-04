(ns game-of-life.patterns
  "Library of well-known Game of Life patterns.
   Each pattern is a set of [x y] coordinate vectors.")

(def glider
  "Glider: a small spaceship that moves diagonally.
   Period 4, moves (1,1) every 4 generations."
  #{[1 0]
    [2 1]
    [0 2] [1 2] [2 2]})

(def blinker
  "Blinker: the simplest oscillator, period 2."
  #{[0 0] [1 0] [2 0]})

(def pulsar
  "Pulsar: a period-3 oscillator."
  #{;; Top horizontal bars
    [2 0] [3 0] [4 0]
    [8 0] [9 0] [10 0]
    ;; Upper section vertical bars
    [0 2] [5 2] [7 2] [12 2]
    [0 3] [5 3] [7 3] [12 3]
    [0 4] [5 4] [7 4] [12 4]
    ;; Middle horizontal bars
    [2 5] [3 5] [4 5]
    [8 5] [9 5] [10 5]
    ;; Lower middle horizontal bars
    [2 7] [3 7] [4 7]
    [8 7] [9 7] [10 7]
    ;; Lower section vertical bars
    [0 8] [5 8] [7 8] [12 8]
    [0 9] [5 9] [7 9] [12 9]
    [0 10] [5 10] [7 10] [12 10]
    ;; Bottom horizontal bars
    [2 12] [3 12] [4 12]
    [8 12] [9 12] [10 12]})

(def gosper-glider-gun
  "Gosper Glider Gun: the first known finite pattern that grows without limit.
   Emits a new glider every 30 generations."
  #{;; Left square
    [0 4] [0 5]
    [1 4] [1 5]
    ;; Left part
    [10 4] [10 5] [10 6]
    [11 3] [11 7]
    [12 2] [12 8]
    [13 2] [13 8]
    [14 5]
    [15 3] [15 7]
    [16 4] [16 5] [16 6]
    [17 5]
    ;; Right part
    [20 2] [20 3] [20 4]
    [21 2] [21 3] [21 4]
    [22 1] [22 5]
    [24 0] [24 1] [24 5] [24 6]
    ;; Right square
    [34 2] [34 3]
    [35 2] [35 3]})

(def lightweight-spaceship
  "Lightweight Spaceship (LWSS): moves horizontally, period 4."
  #{[1 0] [4 0]
    [0 1]
    [0 2] [4 2]
    [0 3] [1 3] [2 3] [3 3]})

(def all-patterns
  "Map of pattern name to pattern data."
  {"glider"    glider
   "blinker"   blinker
   "pulsar"    pulsar
   "gosper"    gosper-glider-gun
   "lwss"      lightweight-spaceship})

(def pattern-aliases
  "Additional name aliases for patterns."
  {"gosperglidergun"     "gosper"
   "gun"                 "gosper"
   "lightweightspaceship" "lwss"
   "spaceship"           "lwss"})

(defn get-by-name
  "Returns a pattern by name (case-insensitive), or nil if not found."
  [name]
  (let [n (clojure.string/lower-case name)
        canonical (get pattern-aliases n n)]
    (get all-patterns canonical)))

(defn all-names
  "Returns all available pattern names."
  []
  (keys all-patterns))
