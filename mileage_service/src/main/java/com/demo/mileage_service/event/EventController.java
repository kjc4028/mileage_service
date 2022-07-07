package com.demo.mileage_service.event;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.demo.mileage_service.common.EventDto;
import com.demo.mileage_service.common.Message;
import com.demo.mileage_service.mileage.MileageEntity;
import com.demo.mileage_service.mileage.MileageService;
import com.demo.mileage_service.place.PlaceEntity;
import com.demo.mileage_service.place.PlaceService;
import com.demo.mileage_service.review.ReviewEntity;
import com.demo.mileage_service.review.ReviewService;
import com.demo.mileage_service.review.AttFile.ReviewPhotoEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class EventController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private MileageService mileageService;

    @Autowired
    private PlaceService placeService;

    @PostMapping("/event")
    @ResponseBody
    public ResponseEntity<Message> eventCall(@RequestBody EventDto eventDto){
        
        HttpHeaders headers = new HttpHeaders();
        Message message = new Message();
        HttpStatus httpStatus = HttpStatus.OK;
        String resultMsg = "";
        String proccessMsg = "";

        List<String> photoArr = new ArrayList<String>();
        if(eventDto.getAttachedPhotoIds() != null){
            photoArr = eventDto.getAttachedPhotoIds();
        }

        PlaceEntity placeEntity = PlaceEntity.builder(eventDto.getPlaceId()).build();

        ReviewEntity reviewEntity = ReviewEntity.builder(eventDto.getReviewId())
                                    .reviewCts(eventDto.getContent())
                                    .reviewPhotoList(new ArrayList<>())
                                    .userId(eventDto.getUserId())
                                    .placeEntity(placeEntity)
                                    .build();

        for(String photoId : photoArr){
            ReviewPhotoEntity reviewPhotoEntity = ReviewPhotoEntity.builder(photoId).build();
            reviewPhotoEntity.setReviewEntity(reviewEntity);
            reviewEntity.getReviewPhotoList().add(reviewPhotoEntity);
        }

        if(eventDto.getType().equals("REVIEW")){
            if(eventDto.getAction().equals("ADD")){
                resultMsg = reviewService.reviewInsert(reviewEntity);
                proccessMsg = "리뷰 등록 완료";
            } else if(eventDto.getAction().equals("MOD")) {
                resultMsg = reviewService.modReview(reviewEntity);
                proccessMsg = "리뷰 수정 완료";
            } else if(eventDto.getAction().equals("DELETE")){
                resultMsg = reviewService.deleteReview(reviewEntity);
                proccessMsg = "리뷰 삭제 완료";
            }
        }

        message.setData(resultMsg);
        message.setMessage(proccessMsg);
        message.setStatus(httpStatus);
        
        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        return new ResponseEntity<Message>(message, headers, httpStatus);
    }

    @GetMapping("/mileage/{userId}")
    @ResponseBody
    public ResponseEntity<Message> selectMileage(EventDto eventDto){
        
        HttpHeaders headers = new HttpHeaders();
        Message message = new Message();
        HttpStatus httpStatus = HttpStatus.OK;
        HashMap<String, Object> resMap = new HashMap<String, Object>();
        ObjectMapper om = new ObjectMapper();
        
        String resData = "";
        String proccessMsg = "";

        MileageEntity mileageEntity = mileageService.findByUserId(eventDto.getUserId());
        
        // resMap.put("userId", mileageEntity.getUserId());
        // resMap.put("point", mileageEntity.getPoint());
        resMap = om.convertValue(mileageEntity, HashMap.class);

        try {
            resData = om.writeValueAsString(resMap);
            proccessMsg = "정상조회";
        } catch (Exception e) {
            proccessMsg = "포인트조회중 문제 발생";
        }

        message.setData(resData);
        message.setMessage(proccessMsg);
        message.setStatus(httpStatus);

        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        return new ResponseEntity<Message>(message, headers, httpStatus);
    }

    @PostMapping("/mileage")
    @ResponseBody
    public ResponseEntity<Message> joinMileage(@RequestBody EventDto eventDto){
        HttpHeaders headers = new HttpHeaders();
        Message message = new Message();
        HttpStatus httpStatus = HttpStatus.OK;
        String resData = "";
        String proccessMsg = "";
        String userId = "";

        userId = eventDto.getUserId();

        if(userId == null){
            proccessMsg = "가입 할 아이디를 입력하세요.";
        } else{
            MileageEntity mileageEntity = MileageEntity.builder(eventDto.getUserId()).point(0).build();
            mileageService.insertMileageInfo(mileageEntity);
            proccessMsg = "ID : " + eventDto.getUserId() + " 가입 성공";
        }

        message.setData(resData);
        message.setMessage(proccessMsg);
        message.setStatus(httpStatus);

        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        return new ResponseEntity<Message>(message, headers, httpStatus);
    }

    @PostMapping("/place")
    @ResponseBody
    public ResponseEntity<Message> insertPlace(@RequestBody PlaceEntity placeDto){
        HttpHeaders headers = new HttpHeaders();
        Message message = new Message();
        HttpStatus httpStatus = HttpStatus.OK;
        String resData = "";
        String proccessMsg = "";


        String uuid = UUID.randomUUID().toString();

        if(placeDto.getPlaceNm() == null){
            proccessMsg = "등록할 장소를 입력하세요.";
        } else {
            PlaceEntity placeEntity = PlaceEntity.builder(uuid).placeNm(placeDto.getPlaceNm()).build();
    
            placeService.insertPlace(placeEntity);
            proccessMsg = "장소등록 성공";
        }

        message.setData(resData);
        message.setMessage(proccessMsg);
        message.setStatus(httpStatus);

        headers.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));

        return new ResponseEntity<Message>(message, headers, httpStatus);
    }

}