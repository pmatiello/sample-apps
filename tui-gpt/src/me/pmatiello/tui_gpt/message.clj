(ns me.pmatiello.tui-gpt.message
  (:require [clojure.spec.alpha :as s]))

(defn new
  [role content tokens]
  {:role role :content content :tokens tokens})

(s/def ::content string?)
(s/def ::role string?)
(s/def ::tokens integer?)

(s/def ::message
  (s/keys :req-un [::content ::role ::tokens]))

(s/fdef new
  :args (s/cat :role ::role :content ::content :tokens ::tokens)
  :ret ::message)
