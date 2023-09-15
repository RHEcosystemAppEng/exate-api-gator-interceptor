package exate.gator.interceptor.services;

import exate.gator.interceptor.content.DatasetPayload;
import exate.gator.interceptor.content.TokenPayload;

/** Payloads creation service used for creating payloads for API-Gator requests. */
public interface PayloadsService {
    /**
     * Create a payload for TOKEN requests to API-Gator.
     * @return a payload ready to be sent for TOKEN requests.
     */
    TokenPayload createTokenPayload();

    /**
     * Create a payload for DATASET requests to API-Gator.
     * @param dataset the dataset is the body from the original service response required to be Gator'ed.
     * @return a payload ready to be sent for DATASET requests.
     */
    DatasetPayload createDatasetPayload(String dataset);
}
