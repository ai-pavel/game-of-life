(ns game-of-life.renderer
  "Console renderer that prints the Game of Life grid to the terminal."
  (:require [game-of-life.grid :as grid]))

(def ^:private alive-char \u2588)  ; Full block
(def ^:private dead-char \space)

(defn render
  "Renders the grid to a string for the given viewport.
   Returns a string representation of the grid."
  [live-cells {:keys [width height offset-x offset-y]
               :or {width 80 height 24 offset-x 0 offset-y 0}}]
  (let [sb (StringBuilder.)]
    (doseq [y (range offset-y (+ offset-y height))]
      (doseq [x (range offset-x (+ offset-x width))]
        (.append sb (if (contains? live-cells [x y]) alive-char dead-char)))
      (when (< y (+ offset-y height -1))
        (.append sb \newline)))
    (.toString sb)))

(defn center-viewport
  "Returns viewport options centered on the current live cells."
  [live-cells width height]
  (if-let [[min-x min-y max-x max-y] (grid/bounds live-cells)]
    (let [center-x (quot (+ min-x max-x) 2)
          center-y (quot (+ min-y max-y) 2)]
      {:width width
       :height height
       :offset-x (- center-x (quot width 2))
       :offset-y (- center-y (quot height 2))})
    {:width width :height height :offset-x 0 :offset-y 0}))

(defn render-to-console
  "Renders the grid to the console, clearing the screen first."
  [live-cells generation viewport]
  (print "\033[H")  ; Move cursor to top-left
  (println (render live-cells viewport))
  (printf "Generation: %d  Population: %d    %n" generation (count live-cells))
  (flush))

(defn animate
  "Animates the grid in the console for the given number of generations."
  [initial-cells & {:keys [generations delay-ms width height]
                    :or {generations Integer/MAX_VALUE
                         delay-ms 100
                         width 80
                         height 24}}]
  (print "\033[2J")  ; Clear screen
  (let [viewport (center-viewport initial-cells width height)]
    (loop [cells initial-cells
           gen 0]
      (when (< gen generations)
        (render-to-console cells gen viewport)
        (Thread/sleep delay-ms)
        (recur (grid/step cells) (inc gen))))))
