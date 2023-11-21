/*
 * Copyright 2013 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.async

import spock.lang.Ignore
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

/**
 * @author Graeme Rocher
 * @since 2.3
 */
class PromiseMapSpec extends Specification {

    void 'Test PromiseMap with mixture of normal entries and promises populated via constructor'() {
        
        when: 'a promise map is used with an onComplete handler'
            def map = new PromiseMap(one: { 1 }, four: 4, eight: { 4 * 2 })
            def result
            map.onComplete { result = it }

        then: 'an appropriately populated map is returned to the onComplete event'
            new PollingConditions().eventually {
                result != null
                result['one'] == 1
                result['four'] == 4
                result['eight'] == 8
            }
    }
    
    void 'Test PromiseMap with mixture of normal entries and promises'() {

        when: 'a promise map is used with an onComplete handler'
            def map = new PromiseMap()
            map['one'] = { 1 }
            map['four'] = 4
            map['eight'] =  { 4 * 2 }
            def result = null
            map.onComplete { result = it }

        then: 'An appropriately populated map is returned to the onComplete event'
            new PollingConditions().eventually {
                result
                result['one'] == 1
                result['four'] == 4
                result['eight'] == 8
            }
    }

    void 'Test that a PromiseMap populates values from promises onComplete'() {

        when:"A promise map is used with an onComplete handler"
            def map = new PromiseMap()
            map['one'] = { 1 }
            map['four'] = { 2 + 2 }
            map['eight'] = { 4 * 2 }
            def result = null
            map.onComplete { result = it }
            sleep 300

        then:'An appropriately populated map is returned to the onComplete event'
            result
            result['one'] == 1
            result['four'] == 4
            result['eight'] == 8
    }

    void 'Test that a PromiseMap triggers onError for an exception and ignores onComplete'() {

        when: 'A promise map is used with an onComplete handler'
            def map = new PromiseMap()
            map['one'] = { 1 }
            map['four'] = { throw new RuntimeException('bad') }
            map['eight'] = { 4 * 2 }
            def result = null
            Throwable err = null
            map.onComplete { result = it }
            map.onError { err = it }
            sleep 300

        then: 'An appropriately populated map is returned to the onComplete event'
            !result
            err
            err.message == 'java.lang.RuntimeException: bad'
    }

    @Ignore('''
        This test fails because the chained call to then does not use the
        map returned from the previous closure. So the same first map
        is returned over and over.
    ''')
    void 'Test PromiseMap with then chaining'() {

        when: 'A promise map is used with then chaining'
            def map = new PromiseMap<String, Integer>()
            map['one'] = { 1 }
            def promise= map.then {
                println it
                it['four'] = 4; it
            }.then {
                println it
                it['eight'] = 8; it
            }
            def result = promise.get()

        then: 'An appropriately populated map is returned to the onComplete event'
            result
            result['one'] == 1
            result['four'] == 4
            result['eight'] == 8
    }
}
