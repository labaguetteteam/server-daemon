(ns server-daemon.core
  (:gen-class)
  (:require [server-daemon.estimates-api :as estimates-api]
            [server-daemon.channels :as channels]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.data.json :as json]))

(use 'overtone.at-at)

(def cli-options
  [["-s" "--serverToken TOKEN" "Server Token"]
   ["-p" "--prod" "Prod Mode"]])

(def pool-task (mk-pool))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]

    (channels/process-channel channels/pipe)

    (if (:prod options)
      (every 120000 #(estimates-api/get-price options) pool-task)
      (do
        (estimates-api/get-price options)
        (loop [] (recur))
        ))))
