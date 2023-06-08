(ns me.pmatiello.sample-apps.gpt-core.openai-api
  (:require [clojure.string :as str]
            [me.pmatiello.openai-api.api :as openai]
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

(defn chat-stream
  [messages callback-fn api-config]
  (let [messages (map #(select-keys % [:role :content]) messages)
        chunks   (->> (openai/chat {:model    "gpt-3.5-turbo"
                                    :messages messages
                                    :stream   true}
                                   api-config)
                      (map :choices)
                      (map first)
                      (map :delta)
                      (map :content)
                      (filter some?))]
    (doseq [chunk chunks]
      (callback-fn chunk))
    (let [resp-text   (str/join chunks)
          resp-tokens (token-count resp-text api-config)]
      (message/new "assistant" resp-text resp-tokens))))
