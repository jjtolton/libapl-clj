(defproject jjtolton/libapl-clj "0.1.1-ALPHA-SNAPSHOT"
  :description "Clojure bindings for GNU APL"
  :url "http://github.com/jjtolton/libapl-clj"
  :plugins [[lein-tools-deps "0.4.5"]]
  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]
  :lein-tools-deps/config {:config-files [:install :user :project]})
