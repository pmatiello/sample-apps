(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'me.pmatiello/tui-gpt)
(def version "0.1.0")
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def src-dirs ["src"])
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean [_]
      (b/delete {:path "target"}))

(defn jar [_]
      (b/write-pom {:class-dir class-dir
                    :lib       lib
                    :version   version
                    :basis     basis
                    :src-dirs  src-dirs})
      (b/copy-dir {:src-dirs   src-dirs
                   :target-dir class-dir})
      (b/compile-clj {:basis     basis
                      :src-dirs  src-dirs
                      :class-dir class-dir})
      (b/uber {:class-dir class-dir
               :uber-file jar-file
               :basis     basis
               :main      'me.pmatiello.sample-apps.tui-gpt.app}))
