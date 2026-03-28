package com.selfinterior.api.visualqa;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockVisionQaClient implements VisionQaClient {
  @Override
  public VisionQaResult analyze(
      String questionText, String processStepKey, SpaceType spaceType, List<String> imagePaths) {
    String normalizedQuestion = questionText == null ? "" : questionText.toLowerCase();
    boolean isLeakRelated =
        normalizedQuestion.contains("누수")
            || normalizedQuestion.contains("결로")
            || normalizedQuestion.contains("곰팡이");
    boolean isElectricalRelated =
        normalizedQuestion.contains("전기")
            || normalizedQuestion.contains("스위치")
            || normalizedQuestion.contains("콘센트");

    RiskLevel riskLevel = RiskLevel.MEDIUM;
    boolean expertRequired = false;
    String possibleCausesText = "시공 상태, 사용 환경, 기존 마감 상태가 함께 영향을 준 것으로 보입니다.";
    String nextChecksText = "같은 위치를 다른 각도에서 한 번 더 촬영하고, 발생 시점과 빈도를 기록하세요.";
    String proceedRecommendationText = "추가 확인 후 관련 공정을 진행하는 것이 안전합니다.";

    if (isLeakRelated) {
      riskLevel = RiskLevel.HIGH;
      expertRequired = true;
      possibleCausesText = "누수 또는 결로 가능성이 있어 배관, 외벽, 환기 상태를 함께 확인해야 합니다.";
      nextChecksText = "비 오는 날 이후 변화, 상부 배관 위치, 실리콘·실금 상태를 추가 확인하세요.";
      proceedRecommendationText = "원인 확인 전 마감 공정 진행은 보수 비용을 키울 수 있어 점검을 권장합니다.";
    } else if (isElectricalRelated) {
      riskLevel = RiskLevel.HIGH;
      expertRequired = true;
      possibleCausesText = "배선 노후, 결선 불량, 사용량 증가가 함께 작용했을 수 있습니다.";
      nextChecksText = "차단기 이력, 해당 회로의 다른 기기 사용 여부, 발열 여부를 추가 확인하세요.";
      proceedRecommendationText = "전기 안전과 관련될 수 있어 전문가 점검 전 확정 판단은 피하는 것이 좋습니다.";
    }

    return new VisionQaResult(
        riskLevel,
        String.format(
            "%s 공간 사진 %d장을 기준으로 표면 상태와 마감 흔적이 관찰됩니다.", spaceType.name(), imagePaths.size()),
        possibleCausesText,
        nextChecksText,
        proceedRecommendationText,
        expertRequired,
        riskLevel == RiskLevel.HIGH ? 76.4 : 62.8);
  }
}
