package com.example.RSW.service;

import com.example.RSW.repository.MemberRepository;
import com.example.RSW.repository.NotificationRepository;
import com.example.RSW.repository.WalkCrewMemberRepository;
import com.example.RSW.vo.Member;
import com.example.RSW.vo.Notification;
import com.example.RSW.vo.WalkCrew;
import com.example.RSW.vo.WalkCrewMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class NotificationService {

	private final NotificationRepository notificationRepository;

	@Autowired
	WalkCrewMemberRepository walkCrewMemberRepository;

	@Autowired
	private MemberService memberService;

	@Autowired
	private WalkCrewMemberService walkCrewMemberService;

	@Autowired
	private WalkCrewService walkCrewService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	public NotificationService(NotificationRepository notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	// 알림 추가 메서드
	public void addNotification(int memberId, int senderId, String type, String title, String link) {
		// 중복 알림 방지
		if (notificationRepository.existsByMemberIdAndTitleAndLink(memberId, title, link)) {
			return;
		}

		// 알림 객체 생성
		Notification notification = new Notification();
		notification.setMemberId(memberId); // 알림을 받을 회원
		notification.setSenderId(senderId); // 알림을 보낸 사람
		notification.setType(type); // 알림 타입
		notification.setTitle(title); // 알림 제목
		notification.setLink(link); // 알림 링크
		notification.setRegDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())); // 알림 등록 시간
		notification.setRead(false); // 알림 상태 (읽지 않음)

		// 알림 데이터베이스에 저장
		notificationRepository.insert(notification);
	}

	// 관리자에게 수의사 인증서 등록 알림 전송
	public void sendNotificationToAdmins(int vetMemberId) {
		// 수의사 이름 가져오기
		Member vetMember = memberService.getMemberById(vetMemberId);
		String vetName = vetMember.getNickname(); // 수의사 이름

		// 관리자 목록 가져오기
		List<Member> admins = memberService.getAdmins(); // 관리자 목록 가져오기

		// 각 관리자에게 알림을 전송
		for (Member admin : admins) {
			String title = vetName + "님이 인증서를 등록하였습니다."; // 알림 제목
			String link = "/adm/article/list"; // 인증서 상세 페이지 링크
			// 알림 전송
			addNotification(admin.getId(), vetMemberId, "VET_CERT_UPLOAD", title, link);
		}
	}

	public void addNotification(Notification notification) {
		notificationRepository.insert(notification); // ✅ insert로 통일
	}

	public List<Notification> getNotificationsByMemberId(int memberId) {
		return notificationRepository.findByMemberIdOrderByRegDateDesc(memberId);
	}

	public boolean markAsRead(int memberId, int notificationId) {
		Notification notification = notificationRepository.findById(notificationId).orElse(null);
		if (notification == null || !notification.getMemberId().equals(memberId)) {
			return false;
		}

		if (notification.isRead()) {
			return true; // 이미 읽음 처리된 경우는 무시
		}

		notification.setRead(true);
		int affectedRows = notificationRepository.update(notification);
		return affectedRows == 1; // ← 실제로 DB 반영되었는지 확인
	}

	public List<Notification> getRecentNotifications(int memberId) {
		return notificationRepository.findByMemberIdOrderByRegDateDesc(memberId);
	}

	public void notifyMember(int memberId, String message, String link) {

		if (notificationRepository.existsByMemberIdAndTitleAndLink(memberId, message, link)) {
			return;
		}

		Notification notification = new Notification();
		notification.setMemberId(memberId);
		notification.setTitle(message);
		notification.setLink(link);
		notification.setRegDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
		notification.setRead(false);

		notificationRepository.insert(notification);
	}

	public int getUnreadCount(int loginedMemberId) {
		return notificationRepository.countUnreadByMemberId(loginedMemberId);
	}

	public boolean markAllAsRead(int loginedMemberId) {
		notificationRepository.updateAllAsReadByMemberId(loginedMemberId);
		return true;
	}

	public Notification findById(int id) {

		return notificationRepository.findById(id).orElse(null);
	}

	public boolean hasUnread(int memberId) {
		return notificationRepository.countUnreadByMemberId(memberId) > 0;
	}

	public boolean deleteById(int id, int memberId) {
		Notification noti = notificationRepository.findById(id).orElse(null);
		if (noti == null || noti.getMemberId() != memberId)
			return false;

		notificationRepository.deleteById(id, memberId);
		return true;
	}

	public boolean deleteByLinkAndTitle(int memberId, String link, String title) {

		return notificationRepository.deleteByLinkAndTitle(memberId, link, title) > 0;
	}

	public void send(int memberId, String title, String link) {
		Notification notification = new Notification();
		notification.setMemberId(memberId);
		notification.setTitle(title);
		notification.setLink(link);

		// ✅ LocalDateTime → Date 변환
		LocalDateTime now = LocalDateTime.now();
		Date regDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

		notification.setRegDate(regDate); // Date 타입에 맞게 세팅
		notification.setRead(false);

		notificationRepository.save(notification);
	}

	public List<Notification> findByMemberId(int memberId) {
		return notificationRepository.findByMemberIdOrderByRegDateDesc(memberId);
	}

	// 관리자 전용
	public void deleteById(int id) {
		notificationRepository.deleteByIdOnlyId(id);
	}

	public void deleteByMemberId(int memberId) {
		notificationRepository.deleteByMemberId(memberId);
	}

	public void sendNotificationToAll(String title, String link, String type, Integer senderId, Integer crewId) {
		if (crewId != null && crewId > 0) {
			// 크루 멤버에게만 전송
			List<Integer> crewMemberIds = walkCrewMemberRepository.intFindMembersByCrewId(crewId);
			for (Integer crewMemberId : crewMemberIds) {
				notificationRepository.insert(
						new Notification(0, crewMemberId, title, link, new Date(), false, null, type, senderId));
				System.out.println("🔔 크루 알림 전송: /topic/notifications/" + crewMemberId + " -> new");
				messagingTemplate.convertAndSend("/topic/notifications/" + crewMemberId, "new");
			}
		} else {
			// 전체 회원에게 전송
			List<Integer> memberIds = memberRepository.getAllMemberIds();
			for (Integer memberId : memberIds) {
				notificationRepository
						.insert(new Notification(0, memberId, title, link, new Date(), false, null, type, senderId));
				System.out.println("🔔 전체 알림 전송: /topic/notifications/" + memberId + " -> new");
				messagingTemplate.convertAndSend("/topic/notifications/" + memberId, "new");
			}
		}

	}

	public void deleteAllByMemberId(int loginedMemberId) {
		notificationRepository.deleteAllByMemberId(loginedMemberId);
	}

	public void deleteByLink(String link) {
		notificationRepository.deleteByLink(link);
	}

	public void sendNotificationToMember(String title, String link, String type, Integer senderId, Integer crewId) {
		List<WalkCrewMember> memberIds = walkCrewMemberService.getMembersByCrewId(crewId);
		for (WalkCrewMember member : memberIds) {
			notificationRepository.insert(
					new Notification(0, member.getMemberId(), title, link, new Date(), false, null, type, senderId));
			System.out.println("🔔 알림 전송: /topic/notifications/" + member.getMemberId() + " -> new");
			messagingTemplate.convertAndSend("/topic/notifications/" + member.getMemberId(), "new");
		}
	}

	// 크루장 알림
	public void notifyCrewLeaderOnRequest(int crewId, int requesterId) {
		WalkCrew crew = walkCrewService.getCrewById(crewId);
		if (crew == null)
			return;

		int leaderId = crew.getLeaderId();
		Member requester = memberService.getMemberById(requesterId);
		String title = requester.getNickname() + "님이 크루 참가를 신청했습니다.";
		String link = "/usr/crewCafe/cafeHome?crewId=" + crewId;

		addNotification(leaderId, requesterId, "CREW_JOIN_REQUEST", title, link);
		messagingTemplate.convertAndSend("/topic/notifications/" + leaderId, "new");
	}

	// 청자 알림
	public void notifyMemberOnCrewAccepted(int crewId, int memberId) {
		WalkCrew crew = walkCrewService.getCrewById(crewId);
		if (crew == null)
			return;

		String title = "신청하신 크루 [" + crew.getTitle() + "]에 수락되었습니다.";
		String link = "/usr/crewCafe/cafeHome?crewId=" + crewId;

		addNotification(memberId, crew.getLeaderId(), "CREW_JOIN_ACCEPTED", title, link);
		messagingTemplate.convertAndSend("/topic/notifications/" + memberId, "new");
	}
}