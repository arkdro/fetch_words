(ns fetch-words.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            ;; [clojure.tools.trace :as trace]
            [fetch-words.process]))

(def cli-options
  ;; An option with a required argument
  [["-w" "--wordlist WORDLIST" "a file with the list of words"
    :validate [#(and
                 (some? %)
                 (string? %))
               "Must be non-empty string"]]
   ["-o" "--outdir OUTDIR" "output dir"
    :validate [#(and
                 (some? %)
                 (string? %))
               "Must be non-empty string"]]
   ["-l" "--levels N" "create N levels of output directories. Default: round(log10(wordlist size))"
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 %)
               "Must be a positive integer"]]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn activate_trace
  []
  ;; (trace/trace-ns 'fetch-words.process)
  )

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
        help (get-in opts [:options :help])]
    (cond
      help (println (get opts :summary))
      errors (println errors)
      :default (fetch-words.process/process_word_list wordlist outdir levels var_arguments)))
  (System/exit 0))
