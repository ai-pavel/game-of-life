(ns game-of-life.grid
  "HashSet-based infinite grid implementing Conway's Game of Life rules.
   Cells are represented as [x y] coordinate vectors.
   The grid is a set of live cell coordinates.")

(defn neighbors
  "Returns the eight neighboring positions for a cell [x y]."
  [[x y]]
  (for [dx [-1 0 1]
        dy [-1 0 1]
        :when (not (and (zero? dx) (zero? dy)))]
    [(+ x dx) (+ y dy)]))

(defn step
  "Advances the grid by one generation using standard Conway rules:
   - A live cell with 2 or 3 neighbors survives.
   - A dead cell with exactly 3 neighbors becomes alive.
   - All other cells die or stay dead."
  [live-cells]
  (let [neighbor-counts (frequencies (mapcat neighbors live-cells))]
    (set (for [[cell cnt] neighbor-counts
               :when (or (= cnt 3)
                         (and (= cnt 2) (contains? live-cells cell)))]
           cell))))

(defn step-n
  "Advances the grid by n generations."
  [live-cells n]
  (nth (iterate step live-cells) n))

(defn bounds
  "Returns the bounding box [min-x min-y max-x max-y] of all live cells,
   or nil if the grid is empty."
  [live-cells]
  (when (seq live-cells)
    (let [xs (map first live-cells)
          ys (map second live-cells)]
      [(reduce min xs) (reduce min ys)
       (reduce max xs) (reduce max ys)])))

(defn add-pattern
  "Adds a pattern (collection of [x y] cells) to the grid at the given offset."
  ([live-cells pattern]
   (add-pattern live-cells pattern 0 0))
  ([live-cells pattern offset-x offset-y]
   (into live-cells
         (map (fn [[x y]] [(+ x offset-x) (+ y offset-y)]))
         pattern)))
