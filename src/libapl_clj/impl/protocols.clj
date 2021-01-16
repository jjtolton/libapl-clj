(ns libapl-clj.impl.protocols
  (:require [libapl-clj.impl.api :as api]
            tech.v3.datatype.pprint))

(deftype PTensor [t]
  Object
  (toString [_]
    (str "\n" (api/avp->string! t))))

(tech.v3.datatype.pprint/implement-tostring-print PTensor)
