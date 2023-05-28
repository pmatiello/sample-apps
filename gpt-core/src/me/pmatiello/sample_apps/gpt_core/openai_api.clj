(ns me.pmatiello.sample-apps.gpt-core.openai-api
  (:require [me.pmatiello.openai-api.api :as openai]
            [me.pmatiello.sample-apps.gpt-core.message :as message]))

(defn config
  [api-key]
  (openai/config :api-key api-key))

(defn token-count
  [text api-config]
  (-> {:model "text-embedding-ada-002" :input text}
      (openai/embedding api-config)
      :usage :prompt-tokens))

(defn chat
  [messages api-config]
  (let [messages (map #(select-keys % [:role :content]) messages)
        api-resp (openai/chat {:model "gpt-3.5-turbo" :messages messages} api-config)
        resp-msg (-> api-resp :choices first :message)
        tokens   (-> api-resp :usage :completion-tokens)]
    (message/new (:role resp-msg) (:content resp-msg) tokens)))
