package com.poc.domain;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poc.persistence.entities.Currency;
import com.poc.persistence.entities.IbanConfigs;
import com.poc.persistence.entities.MasterAccount;
import com.poc.persistence.entities.UserInfo;
import com.poc.persistence.repositories.CurrencyRepository;
import com.poc.persistence.repositories.IbanConfigsRepository;
import com.poc.persistence.repositories.MasterAccountRepository;
import com.poc.persistence.repositories.UserInfoRepository;
import com.poc.web.error_handler.exceptions.NoDataFoundException;
import com.poc.web.models.UserInfoUpdateModel;

@Service
public class UserService {

	@Autowired
	private IbanConfigsRepository ibanConfigsRepository;
	
	@Autowired
	private MasterAccountRepository masterAccountRepository;
	
	@Autowired
	private UserInfoRepository userInfoRepository;
	
	@Autowired
	private CurrencyRepository currencyRepository;
	
	@Transactional
	public void createUser(UserInfo userInfo) {
		
		userInfo = userInfoRepository.save(userInfo);
		
		Currency gbp = currencyRepository.findByCode("GBP");
		
		MasterAccount masterAccount = new MasterAccount();
		masterAccount.setUserInfo(userInfo);
		masterAccount.setBalance(0);
		masterAccount.setCurrency(gbp);
		
		Random random = new Random(System.currentTimeMillis());        
		long generatedNumber = Math.abs(random.nextLong());		        
		String generatedNumberString = generatedNumber + "";
		String accountNumber = generatedNumberString.substring(0, 8);
		masterAccount.setAccountNumber(accountNumber);
		
		masterAccountRepository.save(masterAccount);
	}
	
	public MasterAccount getUser(String nationalId) {
		
		MasterAccount masterAccount = masterAccountRepository.getByNationalId(nationalId);
		if (masterAccount == null) {
			throw new NoDataFoundException();
		}		
		
		IbanConfigs ibanConfigs = ibanConfigsRepository.findOne(1);
		masterAccount.toIban(ibanConfigs);
		
		return masterAccount;
	}
	
	@Transactional
	public void updateUser(UserInfoUpdateModel userInfoUpdateModel) {
		
		UserInfo userInfo = userInfoRepository.findByNationalId(userInfoUpdateModel.getNationalId());
		
		userInfo.setCellPhone(userInfoUpdateModel.getCellPhone());
		userInfo.setEmail(userInfoUpdateModel.getEmail());
		userInfo.setMailingAddress(userInfoUpdateModel.getMailingAddress());
		
		userInfoRepository.save(userInfo);
	}
	
	@Transactional
	public void removeUser(String nationalId) {	
		
		MasterAccount masterAccount = masterAccountRepository.getByNationalId(nationalId);
		masterAccountRepository.delete(masterAccount);
	}
	
	public List<MasterAccount> getUsers(int pageIndex) {
		
		IbanConfigs ibanConfigs = ibanConfigsRepository.findOne(1);
		
		int pageSize = 10;
		Pageable pageRequest = new PageRequest(pageIndex, pageSize);
		List<MasterAccount> page = masterAccountRepository.findAll(pageRequest).getContent();
		
		if (page.isEmpty()) {
			throw new NoDataFoundException();
		}
		
		for (int cursor = 0; cursor < page.size(); cursor++) {
			MasterAccount currentElement = page.get(cursor);
			currentElement.toIban(ibanConfigs);
		}
		
		return page;
	}
	
}
