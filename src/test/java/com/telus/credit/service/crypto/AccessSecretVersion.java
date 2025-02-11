package com.telus.credit.service.crypto;

import java.io.IOException;

/*
 * Copyright 2020 Google LLC
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



// [START secretmanager_access_secret_version]
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;


public class AccessSecretVersion {

  public static void accessSecretVersion() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "cio-creditmgmt-np-15dfbe";
    String secretId = "crypto-key";
    String versionId = "latest";
    accessSecretVersion(projectId, secretId, versionId);
  }

  // Access the payload for the given secret version if one exists. The version
  // can be a version number as a string (e.g. "5") or an alias (e.g. "latest").
  public static void accessSecretVersion(String projectId, String secretId, String versionId)
      throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);

      // Access the secret version.
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

      // Print the secret payload.
      //
      // WARNING: Do not print the secret in a production environment - this
      // snippet is showing how to access the secret material.
      String payload = response.getPayload().getData().toStringUtf8();
      System.out.printf(">>>>>>>>>>>>>>>>>>>> Plaintext: %s\n", payload);
    }
  }
  public static void main(String[] args) throws IOException {
	  try {
		  AccessSecretVersion.accessSecretVersion();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		throw e;
	}
  }
}
// [END secretmanager_access_secret_version]