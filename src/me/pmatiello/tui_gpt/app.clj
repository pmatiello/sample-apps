(ns me.pmatiello.tui-gpt.app
  (:require [clojure.string :as str]
            [me.pmatiello.openai-api.api :as openai]
            [me.pmatiello.tui.core :as tui]))

(def ^:private api-key
  (or (System/getenv "OPENAI_API_KEY")
      (throw (ex-info "Error: $OPENAI_API_KEY is not defined." {}))))

(def ^:private config
  (openai/config :api-key api-key))

(defn ^:private print-intro []
  (tui/println {:style [:bold :fg-blue] :body "tui-gpt"})
  (tui/println "Enter prompts as multiple lines")
  (tui/println "Hit CTRL+D on an empty line to terminate a prompt."))

(defn ^:private read-prompt []
  (tui/println {:style [:bold :fg-purple] :body "prompt>"})
  (let [lines (tui/read-lines)]
    (str/join "\n" lines)))

(defn ^:private print-progress []
  (tui/println {:style [:fg-blue] :body "..."}))

(defn ^:private new-message [role content]
  {:role role :content content})

(defn ^:private chat [messages]
  (let [messages (map #(select-keys % [:role :content]) messages)
        api-resp (openai/chat {:model "gpt-3.5-turbo" :messages messages} config)
        resp-msg (-> api-resp :choices first :message)]
    (new-message (:role resp-msg) (:content resp-msg))))

(defn -main []
  (print-intro)

  (loop []
    (let [prompt (read-prompt)]
      (when-not (empty? prompt)
        (print-progress)
        (let [prompt-msg (new-message "user" prompt)
              messages   [prompt-msg]
              resp-msg   (chat messages)]
          (tui/println {:style [:bold :fg-purple] :body "response>"})
          (tui/println (:content resp-msg))
          (recur))))))
