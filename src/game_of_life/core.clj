(ns game-of-life.core
  "CLI entry point for Conway's Game of Life."
  (:require [game-of-life.grid :as grid]
            [game-of-life.patterns :as patterns]
            [game-of-life.rle :as rle]
            [game-of-life.renderer :as renderer]
            [clojure.string :as str])
  (:gen-class))

(defn- parse-int
  "Parse a string to integer, returning default-val on failure."
  [s default-val]
  (try (Integer/parseInt s)
       (catch Exception _ default-val)))

(defn- parse-opts
  "Simple option parser. Returns a map of option keys to values."
  [args option-defs]
  (loop [args args
         result {}]
    (if (empty? args)
      result
      (let [[arg & rest-args] args
            opt-def (get option-defs arg)]
        (if opt-def
          (if (seq rest-args)
            (recur (rest rest-args) (assoc result (:key opt-def) (first rest-args)))
            result)
          (recur rest-args result))))))

(defn run-command
  "Animate a pattern in the terminal."
  [args]
  (let [pattern-name (or (first args) "glider")
        opts (parse-opts (rest args)
                         {"--delay" {:key :delay}
                          "-d"      {:key :delay}
                          "--generations" {:key :generations}
                          "-g"           {:key :generations}})
        delay-ms (parse-int (:delay opts) 100)
        generations (parse-int (:generations opts) Integer/MAX_VALUE)
        pattern (patterns/get-by-name pattern-name)]
    (if pattern
      (do
        (renderer/animate pattern
                          :generations generations
                          :delay-ms delay-ms)
        (println (str "\nStopped.")))
      (do
        (binding [*out* *err*]
          (println (str "Unknown pattern: " pattern-name))
          (println (str "Available patterns: " (str/join ", " (patterns/all-names)))))))))

(defn load-command
  "Load and animate an RLE file."
  [args]
  (if (empty? args)
    (binding [*out* *err*]
      (println "Usage: load <file.rle> [--delay <ms>] [--generations <n>]"))
    (let [file-path (first args)
          opts (parse-opts (rest args)
                           {"--delay" {:key :delay}
                            "-d"      {:key :delay}
                            "--generations" {:key :generations}
                            "-g"           {:key :generations}})
          delay-ms (parse-int (:delay opts) 100)
          generations (parse-int (:generations opts) Integer/MAX_VALUE)]
      (try
        (let [cells (rle/parse-file file-path)]
          (println (str "Loaded " (count cells) " cells from " file-path))
          (renderer/animate cells
                            :generations generations
                            :delay-ms delay-ms)
          (println (str "\nStopped.")))
        (catch java.io.FileNotFoundException _
          (binding [*out* *err*]
            (println (str "File not found: " file-path))))))))

(defn export-command
  "Export a pattern to RLE format."
  [args]
  (let [pattern-name (or (first args) "glider")
        opts (parse-opts (rest args)
                         {"--output" {:key :output}
                          "-o"       {:key :output}})
        pattern (patterns/get-by-name pattern-name)]
    (if pattern
      (let [rle-str (rle/export pattern)]
        (if-let [output-file (:output opts)]
          (do (spit output-file rle-str)
              (println (str "Exported " pattern-name " to " output-file)))
          (print rle-str)))
      (binding [*out* *err*]
        (println (str "Unknown pattern: " pattern-name))))))

(defn step-command
  "Advance a pattern by N generations and print result."
  [args]
  (let [pattern-name (or (first args) "glider")
        opts (parse-opts (rest args)
                         {"--steps" {:key :steps}
                          "-n"      {:key :steps}})
        steps (parse-int (:steps opts) 1)
        pattern (patterns/get-by-name pattern-name)]
    (if pattern
      (let [result (grid/step-n pattern steps)]
        (println (str "Initial: Generation 0, Population " (count pattern)))
        (println (str "After " steps " step(s): Generation " steps ", Population " (count result)))
        (println)
        (print (rle/export result)))
      (binding [*out* *err*]
        (println (str "Unknown pattern: " pattern-name))))))

(defn list-patterns
  "List all available patterns."
  []
  (println "Available patterns:")
  (doseq [[name pattern] (sort-by key patterns/all-patterns)]
    (printf "  %-20s (%d cells)%n" name (count pattern))))

(defn print-usage
  "Print CLI usage information."
  []
  (println "Conway's Game of Life - CLI")
  (println)
  (println "Usage:")
  (println "  lein run run <pattern> [--delay <ms>] [--generations <n>]")
  (println "  lein run load <file.rle> [--delay <ms>] [--generations <n>]")
  (println "  lein run export <pattern> [-o <file.rle>]")
  (println "  lein run step <pattern> [-n <steps>]")
  (println "  lein run patterns")
  (println)
  (println (str "Patterns: " (str/join ", " (sort (patterns/all-names))))))

(defn -main
  "Main entry point."
  [& args]
  (if (empty? args)
    (print-usage)
    (case (str/lower-case (first args))
      "run"      (run-command (rest args))
      "load"     (load-command (rest args))
      "export"   (export-command (rest args))
      "step"     (step-command (rest args))
      "patterns" (list-patterns)
      "help"     (print-usage)
      "--help"   (print-usage)
      "-h"       (print-usage)
      (do (binding [*out* *err*]
            (println (str "Unknown command: " (first args))))
          (print-usage)))))
