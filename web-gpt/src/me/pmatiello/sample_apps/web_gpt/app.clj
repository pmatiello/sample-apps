(ns me.pmatiello.sample-apps.web-gpt.app
  (:gen-class)
  (:require [markdown.core :as markdown]
            [me.pmatiello.sample-apps.gpt-core.history :as history]
            [me.pmatiello.sample-apps.gpt-core.message :as message]
            [me.pmatiello.sample-apps.gpt-core.openai-api :as openai-api]
            [me.pmatiello.sample-apps.gpt-core.params :as params]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :as r.m.params]
            [selmer.parser :as selmer]))

(def ^:private api-config
  (openai-api/config params/api-key))

(def ^:private history
  (atom []))

(def ^:private current
  (atom nil))

(defn ^:private get-home
  []
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (selmer/render-file "index.html" {})})

(defn ^:private render-content
  [{:keys [content] :as message}]
  (assoc message :content (markdown/md-to-html-string content)))

(defn ^:private get-history
  []
  (let [current-msg  (message/new "assistant" @current -1)
        full-history (if @current (conj @history current-msg) @history)]
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    (selmer/render-file
                "history.html"
                {:history (map render-content full-history)})}))

(defn ^:private add-to-history
  [message]
  (swap! history conj message))

(defn ^:private add-to-current
  [chunk]
  (swap! current str chunk))

(defn ^:private post-prompt
  [params]
  (let [prompt             (get params "prompt")
        prompt-tokens      (openai-api/token-count prompt api-config)
        prompt-msg         (message/new "user" prompt prompt-tokens)
        _                  (add-to-history prompt-msg)
        remaining-tokens   (- params/max-tokens prompt-tokens)
        compressed-history (history/compress @history remaining-tokens)
        messages           (history/append compressed-history prompt-msg)]

    (future
      (-> messages
          (openai-api/chat-stream add-to-current api-config)
          add-to-history)
      (reset! current nil))

    {:status 200
     :header {"Content-Type" "text/html"}
     :body   (selmer/render-file "form.html" {})}))

(def ^:private not-found
  {:status  404
   :headers {"Content-Type" "text/plain"}
   :body    "Not Found"})

(defn ^:private handler*
  [{:keys [request-method uri params] :as request}]
  (case [request-method uri]
    [:get "/"] (get-home)
    [:get "/history"] (get-history)
    [:post "/prompt"] (post-prompt params)
    not-found))

(def handler
  (r.m.params/wrap-params handler*))

(defn -main
  []
  (selmer/cache-off!)
  (jetty/run-jetty handler {:port 3000}))
