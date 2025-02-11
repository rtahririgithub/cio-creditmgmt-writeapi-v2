package com.telus.credit.firestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.collect.Lists;
import com.telus.credit.exceptions.ExceptionConstants;
import com.telus.credit.exceptions.ExceptionHelper;
import com.telus.credit.exceptions.ReadStoreGenericException;
import com.telus.credit.firestore.model.AssesmentDocumentCompact;

@Service
public class AssessmentCollectionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AssessmentCollectionService.class);

	@Autowired
	private Firestore firestore;

	@Value("${assesment.collection.name}")
	private String collectionName;

	private static final String ASSESSMENT_COL = "assessmentMessageCd";
	private static final String FIELD = "customerId";
	private static final int BATCH_SIZE = 10;

	public Optional<Map<String, String>> getCurrentAssesmentCode(List<String> customerIds)
			throws ReadStoreGenericException {
		LOGGER.debug("getCurrentAssesmentCode customerId::{}", customerIds);
		Optional<Map<String, String>> results = Optional.empty();
		if (!CollectionUtils.isEmpty(customerIds)) {
			try {
				if (customerIds.size() > 10) {
					results = getBatchedResults(customerIds);
				} else {
					results = getAssmentDocuments(customerIds);
				}
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("{}: Assessment Query to ReadDB failed for:{} . {}",  ExceptionConstants.STACKDRIVER_METRIC, customerIds, ExceptionHelper.getStackTrace(e));
				throw new ReadStoreGenericException(e);
			}
		}
		return results;
	}

	private Optional<Map<String, String>> getAssmentDocuments(List<String> customerIds) throws InterruptedException, ExecutionException {
		Optional<Map<String, String>> results = Optional.empty();
		QuerySnapshot qSanpshot = firestore.collection(collectionName).select(ASSESSMENT_COL, FIELD)
				.whereIn(FIELD, customerIds).get().get();
		if (!qSanpshot.isEmpty()) {
			Map<String, String> assesmentCodes = qSanpshot.getDocuments().stream().map(doc -> {
				LOGGER.info("Found Assessment Document id:{}", doc.getId());
				return doc.toObject(AssesmentDocumentCompact.class);
			}).collect(Collectors.toMap(AssesmentDocumentCompact::getCustomerId,
					AssesmentDocumentCompact::getAssessmentMessageCd));
			results = Optional.of(assesmentCodes);
		}
		return results;
	}
	
	private Optional<Map<String, String>> getBatchedResults(List<String> customerIds) throws InterruptedException, ExecutionException {
		Optional<Map<String, String>> results = Optional.empty();
		List<List<String>> batches = Lists.partition(customerIds, BATCH_SIZE);
		Map<String, String> fullRes = new HashMap<>();
		for( List<String> ids : batches) {
			Optional<Map<String, String>> batchRes = getAssmentDocuments(ids);
			if(batchRes.isPresent()) {
				fullRes.putAll(batchRes.get());
			}
		}
		if(!CollectionUtils.isEmpty(fullRes)) {
			results = Optional.of(fullRes);
		}
		return results;
	}
}
