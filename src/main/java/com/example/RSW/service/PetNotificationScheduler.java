package com.example.RSW.service;

import com.example.RSW.repository.PetRepository;
import com.example.RSW.repository.PetVaccinationRepository;
import com.example.RSW.vo.Pet;
import com.example.RSW.vo.PetVaccination;
import com.example.RSW.vo.Rq;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PetNotificationScheduler {

    private final PetRepository petRepository;
    private final PetVaccinationRepository vaccinationRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *") // 매일 오전 8시
    public void run() {
        sendBirthdayNotifications();
        sendVaccineNotifications();
    }

    private void sendBirthdayNotifications() {
        List<Pet> upcomingBirthdays = petRepository.findPetsWithBirthdayInDays(List.of(0, 3, 7));
        for (Pet pet : upcomingBirthdays) {
            String title = "🎉 " + pet.getName() + "의 생일이 " + formatDdayText(getBirthdayDday(pet.getBirthDate()));
            int loginMemberId = pet.getMemberId();
            int petId = pet.getId();
            String link = "/usr/pet/petPage?petId="+petId;
            notificationService.addNotification(loginMemberId, petId, "birthday", title, link);
        }
        System.out.println("[알림 스케줄러] 생일 대상 수: " + upcomingBirthdays.size());
    }

    private void sendVaccineNotifications() {
        List<PetVaccination> dueVaccines = vaccinationRepository.findNextDueInDays(List.of(0, 3, 7));
        for (PetVaccination vac : dueVaccines) {
            String title = "💉 " + vac.getPetName() + "의 " + vac.getVaccineName()
                    + " 백신 접종일이 " + formatDdayText(getDday(vac.getNextDueDate()));
            Pet pet = petRepository.getPetsById(vac.getPetId());
            int loginMemberId = pet.getMemberId();
            int petId = vac.getPetId();

            String link = "/usr/pet/petPage?petId="+petId;
            notificationService.addNotification(loginMemberId, petId, "vaccine", title, link);
        }
        System.out.println("[알림 스케줄러] 백신 대상 수: " + dueVaccines.size());
    }

    private int getBirthdayDday(Date birthDate) {
        LocalDate today = LocalDate.now();
        LocalDate birthday = LocalDate.of(today.getYear(), birthDate.getMonth() + 1, birthDate.getDate());

        if (birthday.isBefore(today)) {
            birthday = birthday.plusYears(1);
        }

        return (int) ChronoUnit.DAYS.between(today, birthday);
    }



    private int getDday(Date date) {
        long diff = date.getTime() - System.currentTimeMillis();
        return (int)(diff / (1000 * 60 * 60 * 24));
    }

    private String formatDdayText(int dday) {
        if (dday == 0) return "오늘이에요!";
        else return dday + "일 남았어요!";
    }
}
