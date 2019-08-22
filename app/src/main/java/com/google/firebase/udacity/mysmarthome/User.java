/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.mysmarthome;

public class User {
    private String name;
    private String uid;
    private Boolean isWriter;

    public User() {
    }

    public User(String name, String uid,Boolean isWriter ) {

        this.name = name;
        this.uid = uid;
        this.isWriter = isWriter;
    }

    public Boolean getIsWriter() {
        return isWriter;
    }

    public void setIsWriter(Boolean isWriter) {
        this.isWriter = isWriter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
