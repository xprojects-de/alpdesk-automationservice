package x.utils

import spock.lang.Specification

class MyTestSpecification extends Specification {

    def "should be 3"() {

        given:
        int i = 1
        int u = 2

        when:
        int z = i + u

        then:
        z == 3

    }

}

