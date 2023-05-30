(ns fetch-words.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            ;; [clojure.tools.trace :as trace]
            [fetch-words.process]))

(def cli-options
  ;; An option with a required argument
  [["-w" "--wordlist WORDLIST" "a file with 'INSERT INTO notes VALUES' lines."
    :validate [#(and
                 (some? %)
                 (string? %))
               "Must be non-empty string"]]
   ["-o" "--outdir OUTDIR" "output dir"
    :validate [#(and
                 (some? %)
                 (string? %))
               "Must be non-empty string"]]
   ["-l" "--levels N" "create up to N levels of output sub-directories."
    :default 4
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 %)
               "Must be a positive integer"]]
   ["-i" "--delay-min N" "Min delay to use between requests. Milliseconds."
    :default 0
    :parse-fn #(Integer/parseInt %)
    :validate [#(<= 0 %)
               "Must be a non-negative integer"]]
   ["-a" "--delay-max N" "Max delay to use between requests. Milliseconds."
    :default 0
    :parse-fn #(Integer/parseInt %)
    :validate [#(<= 0 %)
               "Must be a non-negative integer"]]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn activate_trace
  []
  ;; (trace/trace-ns 'fetch-words.process)
  )

(defn get_delay_limits
  [d0 d1]
  (if (<= d0 d1)
    [d0 d1]
    [0 0]))

(defn -main
  "Parse command line arguments and run the generator"
  [& args]
  (activate_trace)
  (let [opts (parse-opts args cli-options)
        var_arguments (get opts :arguments)
        options (get opts :options)
        wordlist (get options :wordlist)
        outdir (get options :outdir)
        levels (get options :levels)
        errors (get opts :errors)
        help (get-in opts [:options :help])
        [delay_min delay_max] (get_delay_limits
                               (get options :delay-min)
                               (get options :delay-max))]
    (cond
      help (println (get opts :summary))
      errors (println errors)
      :default (fetch-words.process/process_word_list
                wordlist
                outdir
                levels
                delay_min
                delay_max)))
  (System/exit 0))
