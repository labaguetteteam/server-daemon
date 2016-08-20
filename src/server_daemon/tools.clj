(ns server-daemon.tools)

(defn uuid [] (str (java.util.UUID/randomUUID)))
(defn timestamp [] (quot (System/currentTimeMillis) 1000))
