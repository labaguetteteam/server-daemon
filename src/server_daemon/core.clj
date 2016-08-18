(ns server-daemon.core
  (:gen-class)
  (:require [server-daemon.estimates-api :as estimates-api]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.data.json :as json]))

(use 'overtone.at-at)

(def cli-options
  [["-s" "--serverToken TOKEN" "Server Token"]
   ["-p" "--prod" "Prod Mode"]
   ])

(def pool-task (mk-pool))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]

    (if (:prod options)
      (every 120000 #(estimates-api/get-price options) pool-task)
      (do
        (estimates-api/get-price options)
        (loop [] (recur))))))
