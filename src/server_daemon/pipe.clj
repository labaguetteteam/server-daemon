(ns server-daemon.pipe
  (:require [clojure.core.async :refer [go-loop go chan put! <!]]))

(def pipe (chan))
