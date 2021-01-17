(ns libapl-clj.impl.helpers
  (:require [libapl-clj.impl.api :as api])
  (:import [java.util UUID]
           [libapl_clj.impl.protocols PTensor]))

(defn random-var-name []
  (clojure.string/replace (str "a" (UUID/randomUUID))  #"-" ""))


(defn pointer? [x]
  (= (str (type x))
     "class com.sun.jna.Pointer"))

(defn scalar? [x]
  ;; todo -- need to get thid of this reflection
  ;;       : this could benefit from a protocol and type extension
  ;;       : perhaps a private method with a try/catch that defaults
  ;;       : to false?
  (or (int? x)
      (double? x)
      (float? x)
      (char? x)))

(defn scalar-pointer? [avp]
  (zero? (api/rank avp)))


(defn drawing!
  "Note: use this for rendering in the REPL only.  Do not 
pass this value around.  The APL pointer is GC'd within 1 second."
  [avp]
  (let [rand-var      (random-var-name)
        rand-val-name (random-var-name)
        ndimensional? (not (scalar-pointer? avp))
        cmd-draw      (format "%s ← 4 ⎕CR %s" rand-val-name rand-var)
        cmd-no-draw   (format "%s ← %s" rand-val-name rand-var)]
    ;; TODO -- there has to be a better way to run system commands
    (when ndimensional?
      (api/atp->ref! rand-var avp)
      (api/run-simple-string! cmd-draw))
    (try
      (if ndimensional?
        (PTensor. (api/ref->avp rand-val-name))
        (PTensor. avp))
      (finally
        ;; todo GC TB
        (future
          (when ndimensional?
            (Thread/sleep 1000)
            (api/run-command-string! "ERASE" rand-var)
            (api/run-command-string! "ERASE" rand-val-name)))))))





