(ns server-daemon.core
  (:gen-class)
  (:require [server-daemon.estimates-api :as estimates-api]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.data.json :as json]))

(def cli-options
  [["-s" "--serverToken TOKEN" "Server Token"]])

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (estimates-api/get-price options)
    (loop [] (recur))))
