package com.poc.web.controllers;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poc.domain.UserService;
import com.poc.web.models.UserInfoCreateModel;
import com.poc.web.models.DetailedUserInfoReadModel;
import com.poc.web.models.BriefUserInfoReadModel;
import com.poc.web.models.UserInfoUpdateModel;
import com.poc.web.validators.Validator;
import com.poc.persistence.entities.MasterAccount;
import com.poc.persistence.entities.UserInfo;

@RequestMapping("v1/users")
@RestController
public class UserController {
	
	@Autowired
	private DateFormat dateFormat;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private Validator validator; 
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Object> createUser(@RequestBody UserInfoCreateModel userInfoCreateModel) throws ParseException {
		
		validator.validate(userInfoCreateModel);	
		
		UserInfo userInfo = new UserInfo();
		userInfo.setName(userInfoCreateModel.getName());
		userInfo.setNationalId(userInfoCreateModel.getNationalId());				
		userInfo.setDateOfBirth(dateFormat.parse(userInfoCreateModel.getDateOfBirth()));
		userInfo.setCellPhone(userInfoCreateModel.getCellPhone());
		userInfo.setEmail(userInfoCreateModel.getEmail());
		userInfo.setMailingAddress(userInfoCreateModel.getMailingAddress());
		
		userService.createUser(userInfo);
		
		return new ResponseEntity<Object>(HttpStatus.CREATED);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "{nationalId}")
	public ResponseEntity<DetailedUserInfoReadModel> getUserByNationalId(@PathVariable String nationalId) {
			
		MasterAccount masterAccount = userService.getUser(nationalId);
		
		DetailedUserInfoReadModel userInfoReadModel = new DetailedUserInfoReadModel();
		userInfoReadModel.setName(masterAccount.getUserInfo().getName());		
		String dateOfBirth = dateFormat.format(masterAccount.getUserInfo().getDateOfBirth());
		userInfoReadModel.setDateOfBirth(dateOfBirth);
		userInfoReadModel.setIban(masterAccount.getIban());
		userInfoReadModel.setBalance(masterAccount.getBalance());
		userInfoReadModel.setCurrency(masterAccount.getCurrency().getCode());		
		userInfoReadModel.setNationalId(masterAccount.getUserInfo().getNationalId());
		userInfoReadModel.setCellPhone(masterAccount.getUserInfo().getCellPhone());
		userInfoReadModel.setEmail(masterAccount.getUserInfo().getEmail());
		userInfoReadModel.setMailingAddress(masterAccount.getUserInfo().getMailingAddress());
		
		return new ResponseEntity<DetailedUserInfoReadModel>(userInfoReadModel, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<Object> updateUser(@RequestBody UserInfoUpdateModel userInfoUpdateModel) {

		validator.validate(userInfoUpdateModel);
		
		userService.updateUser(userInfoUpdateModel);
		
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "{nationalId}")
	public ResponseEntity<Object> removeUser(@PathVariable String nationalId) {
		
		userService.removeUser(nationalId);
		
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<ArrayList<BriefUserInfoReadModel>> getUsers(@RequestParam int pageIndex) {
			
		List<MasterAccount> masterAccounts = userService.getUsers(pageIndex);
		
		ArrayList<BriefUserInfoReadModel> userInfoReadModels = new ArrayList<BriefUserInfoReadModel>();
		for (int cursor = 0; cursor < masterAccounts.size(); cursor++) {
			
			MasterAccount currentElement = masterAccounts.get(cursor);
			
			BriefUserInfoReadModel userInfoReadModel = new BriefUserInfoReadModel();
			userInfoReadModel.setId(currentElement.getId());
			userInfoReadModel.setName(currentElement.getUserInfo().getName());
			userInfoReadModel.setNationalId(currentElement.getUserInfo().getNationalId());
			userInfoReadModel.setIban(currentElement.getIban());
			userInfoReadModel.setBalance(currentElement.getBalance());
			
			userInfoReadModels.add(userInfoReadModel);
		}		
		
		return new ResponseEntity<ArrayList<BriefUserInfoReadModel>>(userInfoReadModels, HttpStatus.OK);
	}
	
}
