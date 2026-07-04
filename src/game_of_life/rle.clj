(ns game-of-life.rle
  "Parses and exports patterns in Run Length Encoded (RLE) format.
   See: https://conwaylife.com/wiki/Run_Length_Encoded"
  (:require [clojure.string :as str]))

(defn parse
  "Parses an RLE string and returns a set of live cells as [x y] vectors."
  [rle-string]
  (let [lines (->> (str/split-lines rle-string)
                   (map str/trim)
                   (remove str/blank?))
        ;; Remove comment lines starting with #
        data-lines (remove #(str/starts-with? % "#") lines)
        ;; Skip header line if present (x = ..., y = ...)
        data-lines (if (and (seq data-lines)
                            (re-find #"(?i)^x\s*=" (first data-lines)))
                     (rest data-lines)
                     data-lines)
        ;; Concatenate remaining lines
        data (str/join "" data-lines)]
    (loop [chars (seq data)
           x 0
           y 0
           run-count 0
           cells #{}]
      (if (or (empty? chars) (= (first chars) \!))
        cells
        (let [c (first chars)]
          (cond
            ;; Digit: accumulate run count
            (Character/isDigit c)
            (recur (rest chars) x y (+ (* run-count 10) (Character/digit c 10)) cells)

            ;; Dead cells
            (= c \b)
            (let [count (if (zero? run-count) 1 run-count)]
              (recur (rest chars) (+ x count) y 0 cells))

            ;; Live cells
            (= c \o)
            (let [count (if (zero? run-count) 1 run-count)
                  new-cells (into cells (map (fn [i] [(+ x i) y]) (range count)))]
              (recur (rest chars) (+ x count) y 0 new-cells))

            ;; End of row
            (= c \$)
            (let [count (if (zero? run-count) 1 run-count)]
              (recur (rest chars) 0 (+ y count) 0 cells))

            ;; Skip other characters (whitespace, etc.)
            :else
            (recur (rest chars) x y run-count cells)))))))

(defn parse-file
  "Parses an RLE file and returns the set of live cells."
  [file-path]
  (parse (slurp file-path)))

(defn export
  "Exports a set of live cells to RLE format string."
  [cells]
  (if (empty? cells)
    "x = 0, y = 0, rule = B3/S23\n!\n"
    (let [cell-set (set cells)
          min-x (reduce min (map first cell-set))
          min-y (reduce min (map second cell-set))
          max-x (reduce max (map first cell-set))
          max-y (reduce max (map second cell-set))
          width (inc (- max-x min-x))
          height (inc (- max-y min-y))
          header (str "x = " width ", y = " height ", rule = B3/S23\n")
          ;; Build RLE data row by row
          data (StringBuilder.)]
      (doseq [row (range min-y (inc max-y))]
        (loop [col min-x]
          (when (<= col max-x)
            (let [alive? (contains? cell-set [col row])
                  tag (if alive? \o \b)
                  ;; Count run of same state
                  run (loop [c col cnt 0]
                        (if (and (<= c max-x)
                                 (= (contains? cell-set [c row]) alive?))
                          (recur (inc c) (inc cnt))
                          cnt))]
              ;; Omit trailing dead cells on each row
              (when-not (and (not alive?) (> (+ col run) max-x))
                (if (= run 1)
                  (.append data tag)
                  (do (.append data run)
                      (.append data tag))))
              (recur (+ col run)))))
        (when (< row max-y)
          (.append data "$")))
      (.append data "!")
      ;; Wrap data lines at 70 characters
      (let [data-str (.toString data)
            wrapped (->> (partition-all 70 data-str)
                         (map #(apply str %))
                         (str/join "\n"))]
        (str header wrapped "\n")))))

(defn export-to-file
  "Exports cells to an RLE file."
  [cells file-path]
  (spit file-path (export cells)))
