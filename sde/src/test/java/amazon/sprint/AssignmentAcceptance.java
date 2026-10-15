package amazon.sprint;

import static amazon.sprint.ExerciseSet.*;
import java.util.*;
import java.util.function.Supplier;

/** Dependency-free instructor/learner acceptance runner; supports an external solution class. */
public final class AssignmentAcceptance {
    private static int cases, passed, todos, failures;
    private static int selectedDay;
    private static boolean daySpecified;
    private static void check(String name, Supplier<?> actual, Object expected) {
        if (selectedDay != 0 && !name.startsWith(String.format("SDE D%02d ", selectedDay))) return;
        cases++;
        try {
            Object value = actual.get();
            if (Objects.deepEquals(value, expected)) { passed++; return; }
            failures++; System.out.println("FAIL " + name + ": expected " + expected + ", got " + value);
        } catch (UnsupportedOperationException e) {
            if (e.getMessage() != null && e.getMessage().contains("Learner TODO")) {
                todos++; System.out.println("TODO " + name + ": " + e.getMessage());
            } else { failures++; System.out.println("ERROR " + name + ": " + e); }
        } catch (Throwable e) { failures++; System.out.println("ERROR " + name + ": " + e); }
    }
    private static ListNode list(String... values) {
        ListNode head=null; for(int i=values.length-1;i>=0;i--) head=new ListNode(values[i],head); return head;
    }
    private static List<String> values(ListNode n) {
        List<String> x=new ArrayList<>(); int guard=0; while(n!=null && guard++<100){x.add(n.value);n=n.next;} return x;
    }
    private static Map<String,String> map(String... kv) {
        Map<String,String> m=new LinkedHashMap<>(); for(int i=0;i<kv.length;i+=2)m.put(kv[i],kv[i+1]); return m;
    }
    private static ExerciseSet load(String implementationClass) throws Exception {
        if(implementationClass==null)return new LearnerExercises();
        return (ExerciseSet)Class.forName(implementationClass).getDeclaredConstructor().newInstance();
    }
    public static void main(String[] args) throws Exception {
        String implementationClass=null;
        for(String arg:args) {
            if(arg.startsWith("--day=")) { selectedDay=Integer.parseInt(arg.substring(6)); daySpecified=true; }
            else implementationClass=arg;
        }
        if(daySpecified&&(selectedDay<1||selectedDay>30)) throw new IllegalArgumentException("--day must be between 1 and 30");
        ExerciseSet s=load(implementationClass);
        check("SDE D01 first normalized email wins",()->s.deduplicateRegistrations(List.of(
            new Registration(" A@X.com ","1",Map.of("market","IN")),new Registration("b@x.com","2",Map.of()),new Registration("a@x.com","3",Map.of()))),List.of(
            new Registration(" A@X.com ","1",Map.of("market","IN")),new Registration("b@x.com","2",Map.of())));
        check("SDE D01 empty",()->s.deduplicateRegistrations(List.of()),List.of());
        check("SDE D01 whitespace/case duplicate",()->s.deduplicateRegistrations(List.of(new Registration(" X@Y.COM ","1",Map.of()),new Registration("x@y.com","2",Map.of()))),List.of(new Registration(" X@Y.COM ","1",Map.of())));

        check("SDE D02 sliding window",()->s.longestRequestWindow(List.of(2,1,3,1,1),4),2);
        check("SDE D02 zero budget",()->s.longestRequestWindow(List.of(5,1),0),0);
        check("SDE D02 empty",()->s.longestRequestWindow(List.of(),0),0);

        check("SDE D03 balanced",()->s.balancedExpression("start({[x]})"),true);
        check("SDE D03 crossing types",()->s.balancedExpression("([)]"),false);
        check("SDE D03 unclosed",()->s.balancedExpression("abc("),false);

        check("SDE D04 duplicate lower bound",()->s.eligibilityLowerBound(List.of(1,3,3,8),3),1);
        check("SDE D04 beyond end",()->s.eligibilityLowerBound(List.of(2,4),9),2);
        check("SDE D04 empty",()->s.eligibilityLowerBound(List.of(),1),0);

        check("SDE D05 reverse links",()->values(s.reverseRequestList(list("r1","r2","r3"))),List.of("r3","r2","r1"));
        check("SDE D05 one node",()->values(s.reverseRequestList(list("only"))),List.of("only"));
        check("SDE D05 null",()->values(s.reverseRequestList(null)),List.of());

        StepNode tree=new StepNode("a",List.of(new StepNode("b",List.of()),new StepNode("c",List.of(new StepNode("d",List.of())))));
        check("SDE D06 BFS",()->s.breadthFirstStepIds(tree),List.of("a","b","c","d"));
        check("SDE D06 null",()->s.breadthFirstStepIds(null),List.of());
        check("SDE D06 wide levels",()->s.breadthFirstStepIds(new StepNode("x",List.of(new StepNode("y",List.of()),new StepNode("z",List.of())))),List.of("x","y","z"));

        check("SDE D07 top-k ties",()->s.prioritizedReviewIds(List.of(new Review("s2",8),new Review("s1",8),new Review("s3",2)),2),List.of("s1","s2"));
        check("SDE D07 empty",()->s.prioritizedReviewIds(List.of(),3),List.of());
        check("SDE D07 k zero",()->s.prioritizedReviewIds(List.of(new Review("s",1)),0),List.of());

        check("SDE D08 touching intervals",()->s.mergeComplianceWindows(List.of(new Interval(1,3),new Interval(2,4),new Interval(7,8))),List.of(new Interval(1,4),new Interval(7,8)));
        check("SDE D08 empty",()->s.mergeComplianceWindows(List.of()),List.of());
        check("SDE D08 touching boundary",()->s.mergeComplianceWindows(List.of(new Interval(2,3),new Interval(1,2))),List.of(new Interval(1,3)));

        check("SDE D09 lexical topological order",()->s.prerequisiteOrder(List.of("id","tax","profile"),List.of(new Dependency("id","profile"),new Dependency("tax","profile"))),List.of("profile","id","tax"));
        check("SDE D09 cycle",()->s.prerequisiteOrder(List.of("a","b"),List.of(new Dependency("a","b"),new Dependency("b","a"))),null);
        check("SDE D09 disconnected lexical",()->s.prerequisiteOrder(List.of("z","a"),List.of()),List.of("a","z"));

        check("SDE D10 unique combinations",()->s.uniqueRuleCombinations(List.of(1,2,2,3),4),List.of(List.of(1,3),List.of(2,2)));
        check("SDE D10 no result",()->s.uniqueRuleCombinations(List.of(2),1),List.of());
        check("SDE D10 duplicate candidates",()->s.uniqueRuleCombinations(List.of(1,1,1),2),List.of(List.of(1,1)));

        check("SDE D11 trie prefix",()->s.triePrefixFields(List.of("TaxId","TaxRegion","Address"),"tax"),List.of("TaxId","TaxRegion"));
        check("SDE D11 no matches",()->s.triePrefixFields(List.of("TaxId"),"z"),List.of());
        check("SDE D11 empty prefix",()->s.triePrefixFields(List.of("B","a","AA"),""),List.of("AA","B","a"));

        check("SDE D12 DP min path",()->s.minimumOnboardingCost(List.of(10,15,20)),15);
        check("SDE D12 DP longer path",()->s.minimumOnboardingCost(List.of(1,100,1,1,1,100,1,1,100,1)),6);
        check("SDE D12 empty",()->s.minimumOnboardingCost(List.of()),0);

        check("SDE D13 retry priority",()->s.retryOrder(List.of(new RetryTask("b",2,1),new RetryTask("a",2,3),new RetryTask("c",1,0))),List.of("c","a","b"));
        check("SDE D13 empty",()->s.retryOrder(List.of()),List.of());
        check("SDE D13 deterministic tie",()->s.retryOrder(List.of(new RetryTask("z",0,1),new RetryTask("a",0,1))),List.of("a","z"));

        check("SDE D14 connected accounts",()->s.duplicateAccountComponents(List.of("a","b","c"),List.of(new AccountLink("a","b"),new AccountLink("b","c"))),1);
        check("SDE D14 isolated accounts",()->s.duplicateAccountComponents(List.of("a","b"),List.of()),2);
        check("SDE D14 self edge",()->s.duplicateAccountComponents(List.of("a"),List.of(new AccountLink("a","a"))),1);

        check("SDE D15 first idempotency wins",()->s.firstIdempotencyResults(List.of(new IdempotencyEvent("k","4"),new IdempotencyEvent("k","9"),new IdempotencyEvent("q","2"))),Arrays.asList("4",null,"2"));
        check("SDE D15 empty",()->s.firstIdempotencyResults(List.of()),List.of());
        check("SDE D15 repeated key",()->s.firstIdempotencyResults(List.of(new IdempotencyEvent("x","a"),new IdempotencyEvent("x","a"))),Arrays.asList("a",null));

        check("SDE D16 LRU eviction",()->s.simulatePolicyCache(2,List.of(new CacheOperation("put","a",1),new CacheOperation("put","b",2),new CacheOperation("get","a",null),new CacheOperation("put","c",3),new CacheOperation("get","b",null))),Arrays.asList(1,null));
        check("SDE D16 miss",()->s.simulatePolicyCache(1,List.of(new CacheOperation("get","x",null))),Arrays.asList((Integer)null));
        check("SDE D16 update moves key",()->s.simulatePolicyCache(2,List.of(new CacheOperation("put","a",1),new CacheOperation("put","b",2),new CacheOperation("put","a",3),new CacheOperation("put","c",4),new CacheOperation("get","a",null))),List.of(3));

        check("SDE D17 distinct versions",()->s.hasConflictingApplicationWrite(List.of(new VersionedUpdate("a",1),new VersionedUpdate("b",1),new VersionedUpdate("a",2))),false);
        check("SDE D17 same version conflict",()->s.hasConflictingApplicationWrite(List.of(new VersionedUpdate("a",1),new VersionedUpdate("a",1))),true);
        check("SDE D17 empty",()->s.hasConflictingApplicationWrite(List.of()),false);

        check("SDE D18 capped backoff",()->s.boundedRetryDelays(4,2,7),List.of(2,4,7,7));
        check("SDE D18 zero attempts",()->s.boundedRetryDelays(0,1,8),List.of());
        check("SDE D18 cap below base",()->s.boundedRetryDelays(2,5,3),List.of(3,3));

        Map<String,String> safe=map("screen","home","action","open");
        check("SDE D20 allowlisted analytics",()->s.allowlistedAnalytics(safe,Set.of("screen","action")),safe);
        check("SDE D20 forbidden key rejected",()->s.allowlistedAnalytics(map("email","x","screen","home"),Set.of("screen")),null);
        check("SDE D20 hidden forbidden substring rejected",()->s.allowlistedAnalytics(map("custom_token","x","screen","home"),Set.of("screen")),null);

        check("SDE D21 missing country fields",()->s.unsupportedCountryFields(List.of(new CountryRequirements("IN",List.of("tax_id","address")),new CountryRequirements("US",List.of("tax_id"))),Set.of("tax_id")),Map.of("IN",List.of("address")));
        check("SDE D21 no requirements",()->s.unsupportedCountryFields(List.of(),Set.of()),Map.of());
        check("SDE D21 all supported",()->s.unsupportedCountryFields(List.of(new CountryRequirements("CA",List.of("tax"))),Set.of("tax")),Map.of());

        check("SDE D22 deterministic rules",()->s.evaluateCompliance(List.of(new Rule("tax","tax_id"),new Rule("address","address")),Map.of("tax_id","T-1")),List.of(new RuleResult("tax","PASS"),new RuleResult("address","FAIL")));
        check("SDE D22 empty rules",()->s.evaluateCompliance(List.of(),Map.of()),List.of());
        check("SDE D22 empty evidence fails",()->s.evaluateCompliance(List.of(new Rule("a","field")),Map.of("field","")),List.of(new RuleResult("a","FAIL")));

        check("SDE D23 outbox ordered per aggregate",()->s.outboxSequencesIncrease(List.of(new OutboxEvent("a",1),new OutboxEvent("b",1),new OutboxEvent("a",2))),true);
        check("SDE D23 duplicate sequence fails",()->s.outboxSequencesIncrease(List.of(new OutboxEvent("a",2),new OutboxEvent("a",2))),false);
        check("SDE D23 interleaving",()->s.outboxSequencesIncrease(List.of(new OutboxEvent("a",2),new OutboxEvent("b",4),new OutboxEvent("a",3))),true);

        check("SDE D24 availability",()->s.serviceAvailabilityPercent(99,100),99.0);
        check("SDE D24 zero total",()->s.serviceAvailabilityPercent(0,0),100.0);
        check("SDE D24 round two decimals",()->s.serviceAvailabilityPercent(1,3),33.33);

        check("SDE D25 allowlisted strings",()->s.validateAssistedExtraction(Map.of("name","A","risk",4,"address","X"),Set.of("name","address")),new ExtractionResult(map("name","A","address","X"),List.of("risk")));
        check("SDE D25 empty",()->s.validateAssistedExtraction(Map.of(),Set.of()),new ExtractionResult(Map.of(),List.of()));
        check("SDE D25 non-string rejected",()->s.validateAssistedExtraction(Map.of("name",1),Set.of("name")),new ExtractionResult(Map.of(),List.of("name")));

        check("SDE D26 response categories",()->s.classifyRegistrationResponses(List.of(201,409,503,400)),List.of("success","conflict","retryable","permanent"));
        check("SDE D26 empty",()->s.classifyRegistrationResponses(List.of()),List.of());
        check("SDE D26 statuses",()->s.classifyRegistrationResponses(List.of(200,429,500)),List.of("success","retryable","retryable"));

        check("SDE D27 top-k scores",()->s.highestReviewScores(List.of(5,1,9,3),2),List.of(9,5));
        check("SDE D27 k zero",()->s.highestReviewScores(List.of(1),0),List.of());
        check("SDE D27 duplicate scores",()->s.highestReviewScores(List.of(4,4,4),2),List.of(4,4));

        check("SDE D28 minimum window",()->s.shortestWindowAtLeast(List.of(2,3,1,2,4,3),7),2);
        check("SDE D28 no window",()->s.shortestWindowAtLeast(List.of(1,1),5),0);
        check("SDE D28 one item",()->s.shortestWindowAtLeast(List.of(8),8),1);

        check("SDE D29 token bucket",()->s.acceptWithinSellerTokenBuckets(2,0.1,List.of(new TokenBucketRequest("A",0),new TokenBucketRequest("A",0),new TokenBucketRequest("A",0),new TokenBucketRequest("A",10))),List.of(true,true,false,true));
        check("SDE D29 fractional refill",()->s.acceptWithinSellerTokenBuckets(1,1,List.of(new TokenBucketRequest("A",0),new TokenBucketRequest("A",0.5),new TokenBucketRequest("A",1))),List.of(true,false,true));
        check("SDE D29 buckets isolated by seller",()->s.acceptWithinSellerTokenBuckets(1,0,List.of(new TokenBucketRequest("A",0),new TokenBucketRequest("B",0),new TokenBucketRequest("A",1))),List.of(true,true,false));

        check("SDE D30 weakest topic",()->s.weakestPracticeTopics(List.of(new PracticeAttempt("graphs",1,5),new PracticeAttempt("strings",4,5),new PracticeAttempt("graphs",1,5))),List.of("graphs"));
        check("SDE D30 weakest tie",()->s.weakestPracticeTopics(List.of(new PracticeAttempt("DP",1,2),new PracticeAttempt("Greedy",2,4))),List.of("DP","Greedy"));
        check("SDE D30 zero-total topic",()->s.weakestPracticeTopics(List.of(new PracticeAttempt("empty",0,0))),List.of("empty"));

        System.out.printf("Java SDE acceptance: %d cases, %d passed, %d TODO, %d failed.%n",cases,passed,todos,failures);
        if(todos>0 || failures>0) System.exit(1);
    }
}
