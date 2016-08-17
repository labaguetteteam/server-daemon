(ns server-daemon.core
  (:gen-class)
  (:require [org.httpkit.client :as http]))

(def options {
              ; :timeout 200
              ; :basic-auth ["user" "pass"]
              ; :query-params {:param "value" :param2 ["value1" "value2"]}
              ; :user-agent "User-Agent-string"
              :headers {"Authorization" "Token 0v0MQyxN4SXhYkpPQMWy9crK7mFE7UViePyBvxMN"}})

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Run")
  (http/get "https://api.uber.com/v1/products?latitude=37.7759792&longitude=-122.41823" options
          (fn [{:keys [status headers body error]}] ;; asynchronous response handling
            (if error
              (println "Failed, exception is " error)
              (do
                (println "Async HTTP GET: " status)
                (println "Body: " body)))))
  (loop [] (recur)))
