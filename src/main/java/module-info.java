module fr.osallek.clausewitzparser {
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j.slf4j;

    exports fr.osallek.clausewitzparser.common;
    exports fr.osallek.clausewitzparser.model;
    exports fr.osallek.clausewitzparser;
}
