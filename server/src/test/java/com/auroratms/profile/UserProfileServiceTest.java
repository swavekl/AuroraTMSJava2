package com.auroratms.profile;

import com.auroratms.AbstractServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserProfileServiceTest extends AbstractServiceTest {

    @Autowired
    private UserProfileService userProfileService;

    @Test
    public void testListingByProfileIdsSmall () {
        String [] arr = {
                "00uy335bvq1ty3vgr0h7", "00uy3357gpIJzWdoN0h7", "00uy335d68Cw8pcCS0h7", "00uy3354biWw4ujXA0h7", "00uy335093X2LygKv0h7", "00uy3359ekH56cRf20h7", "00uy335d7qwg1EbIf0h7", "00uy335elouLGwllC0h7", "00uy3359d3jpZPTlI0h7", "00uy3350eaVGZ2HZG0h7"
        };
        List<String> profileIds = Arrays.asList(arr);
        long start = System.currentTimeMillis();
        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);
        long end = System.currentTimeMillis();
        System.out.println("duration for 10 = " + (end - start));
        assertEquals (arr.length, userProfiles.size(), "wrong number of profiles retrieved");
    }

    @Test
    public void testListingByProfileIds () {
        long start = System.currentTimeMillis();
        String [] arr = {
                "00uy335bvq1ty3vgr0h7", "00uy3357gpIJzWdoN0h7", "00uy335d68Cw8pcCS0h7", "00uy3354biWw4ujXA0h7", "00uy335093X2LygKv0h7", "00uy3359ekH56cRf20h7", "00uy335d7qwg1EbIf0h7", "00uy335elouLGwllC0h7", "00uy3359d3jpZPTlI0h7", "00uy3350eaVGZ2HZG0h7",
                "00uy3352zikyDT5Lu0h7", "00uy3351a4Qbvm3Lq0h7", "00uy3353gu4o1oll80h7", "00uy335d9wyYX6gau0h7", "00uy3353bjcwj8cp00h7", "00uy334yi8zirhriQ0h7", "00uy33599iIULBe1I0h7", "00uy335b3ihe226mj0h7", "00uy335309k7Z5b7V0h7", "00uy334ygquizMC7S0h7",
                "00uy335aruHnTcg6I0h7", "00uy335cee9aY7nrQ0h7", "00uy33538aI9JVwrW0h7", "00uy334ymiY4f51bW0h7", "00uy335bgzKAJDkLg0h7", "00uy334yet1s48amI0h7", "00uy3359iaLGwWjY70h7", "00uy335bqtCIi4LGw0h7", "00uy335cjblDZwLLc0h7", "00uy3353g4KxljfvW0h7",
                "00uy335bm1j28UVGk0h7", "00uy334zt5Apk0XCT0h7", "00uy335em99oCDSCb0h7", "00uy3354wgguTrsiO0h7", "00uy335ai6PqdZew40h7", "00uy335cqn2bnJwZe0h7", "00uy3359fbQYX1tv90h7", "00uy33580aGP0WvT70h7", "00uy335d70eXX9IqU0h7", "00uwbdqm3ded2Crth0h7",
                "00uy3352yqmqmHNi40h7", "00uy3357mfiHcT9nY0h7", "00uy335ci2230ijrH0h7", "00uy3350caMllwnLo0h7", "00uy334ztv6FM9zjp0h7", "00uy335b8l16qEBiT0h7", "00uy33517igRauMdC0h7", "00uy3354u2hwwmbyQ0h7", "00uy335bbrCFySdTz0h7", "00uy335cbt0qJfrFp0h7",
                "00uy335ctmCJvOfpK0h7", "00uy33582vD22G8dQ0h7", "00uynxa0juAZvdN2H0h7", "00uy3354k7mIxT81G0h7", "00uy33534kllqO4TF0h7", "00uy3351ihDMtpbx70h7", "00uy3352rwv2VlurE0h7", "00uy33533t98vOX640h7", "00uy335agrxaFRMIT0h7", "00uy3357pbFTxO0B30h7",
                "00uy334zvlQ4tBKpv0h7", "00uy3351ffeTcgUHa0h7", "00uy3351h4VjLO8mf0h7", "00uy33508b2H6m1az0h7"
        };
        List<String> profileIds = Arrays.asList(arr);
        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);
        long end = System.currentTimeMillis();
        System.out.println("duration for 64 = " + (end - start));
        assertEquals (arr.length, userProfiles.size(), "wrong number of profiles retrieved");
    }

    @Test
    public void testListingByProfileIds300Plus () {
        String [] arr = {"00uy335bvq1ty3vgr0h7", "00uy3357gpIJzWdoN0h7", "00uy335d68Cw8pcCS0h7", "00uy3354biWw4ujXA0h7", "00uy335093X2LygKv0h7", "00uy3359ekH56cRf20h7", "00uy335d7qwg1EbIf0h7", "00uy335elouLGwllC0h7", "00uy3359d3jpZPTlI0h7", "00uy3350eaVGZ2HZG0h7", "00uy3352zikyDT5Lu0h7", "00uy3351a4Qbvm3Lq0h7", "00uy3353gu4o1oll80h7", "00uy335d9wyYX6gau0h7", "00uy3353bjcwj8cp00h7", "00uy334yi8zirhriQ0h7", "00uy33599iIULBe1I0h7", "00uy335b3ihe226mj0h7", "00uy335309k7Z5b7V0h7", "00uy334ygquizMC7S0h7", "00uy335aruHnTcg6I0h7", "00uy335cee9aY7nrQ0h7", "00uy33538aI9JVwrW0h7", "00uy334ymiY4f51bW0h7", "00uy335bgzKAJDkLg0h7", "00uy334yet1s48amI0h7", "00uy3359iaLGwWjY70h7", "00uy335bqtCIi4LGw0h7", "00uy335cjblDZwLLc0h7", "00uy3353g4KxljfvW0h7", "00uy335bm1j28UVGk0h7", "00uy334zt5Apk0XCT0h7", "00uy335em99oCDSCb0h7", "00uy3354wgguTrsiO0h7", "00uy335ai6PqdZew40h7", "00uy335cqn2bnJwZe0h7", "00uy3359fbQYX1tv90h7", "00uy33580aGP0WvT70h7", "00uy335d70eXX9IqU0h7", "00uwbdqm3ded2Crth0h7", "00uy3352yqmqmHNi40h7", "00uy3357mfiHcT9nY0h7", "00uy335ci2230ijrH0h7", "00uy3350caMllwnLo0h7", "00uy334ztv6FM9zjp0h7", "00uy335b8l16qEBiT0h7", "00uy33517igRauMdC0h7", "00uy3354u2hwwmbyQ0h7", "00uy335bbrCFySdTz0h7", "00uy335cbt0qJfrFp0h7", "00uy335ctmCJvOfpK0h7", "00uy33582vD22G8dQ0h7", "00uynxa0juAZvdN2H0h7", "00uy3354k7mIxT81G0h7", "00uy33534kllqO4TF0h7", "00uy3351ihDMtpbx70h7", "00uy3352rwv2VlurE0h7", "00uy33533t98vOX640h7", "00uy335agrxaFRMIT0h7", "00uy3357pbFTxO0B30h7", "00uy334zvlQ4tBKpv0h7", "00uy3351ffeTcgUHa0h7", "00uy3351h4VjLO8mf0h7", "00uy33508b2H6m1az0h7", "00uy3353d8zRr9dAb0h7", "00uy3357vbMGHk6QG0h7", "00uy3351eiI5ylB4n0h7", "00uy3354ohstlkmM20h7", "00uy3352x8QzNtlFk0h7", "00uy33591cqddTNPN0h7", "00uy3353c3khVQfCB0h7", "00uy3351k9v7S4b1A0h7", "00uy3359c0qiW6TMR0h7", "00uy335bfqZQfk8Jn0h7", "00uy3359az3p95jXO0h7", "00uy335c0iFe2lFmV0h7", "00uy335dc2eW4izWh0h7", "00uy335dam0CnIflS0h7", "00uy335dcrsBhr0VU0h7", "00uy3357ujBbmKAOA0h7", "00uy335cwnuwDpjOv0h7", "00uy3354d0oVxeCcX0h7", "00uy3357xapcHiKz50h7", "00uy334zyx3bISLcb0h7", "00uy33595uHYDL6nQ0h7", "00uy335ehz6Dd0KE10h7", "00uy335cls690qfkZ0h7", "00uy335at9RwkFeIv0h7", "00uy334yosnwut9j30h7", "00uy33596xSOg4Krp0h7", "00uy334yqnrEkCUiB0h7", "00uy335519iYRhvqC0h7", "00uy335d2mLRF6rGD0h7", "00uy334zxg0bogY6d0h7", "00uy335dh7ZeG9AhR0h7", "00uy3354sfY5Wq7PM0h7", "00uy335394pRXQ5ZQ0h7", "00uy3354zaYFSbdIN0h7", "00uy3357lqGA4h8zD0h7", "00uy335d4st04F4GZ0h7", "00uy3352r4SpFuCjU0h7", "00uy334yt41kur1d40h7", "00uy3351rtKyxbSD10h7", "00uy33505fJvfuMMT0h7", "00uy3351jk8Xik1px0h7", "00uy33535afDWIsjH0h7", "00uy3354xvTpMxmzx0h7", "00uy335ejfQJulxwJ0h7", "00uy335by8BcPTtyc0h7", "00uy3357kwXOjM4ph0h7", "00uy3357wpbBIsg0P0h7", "00uy3357g09ieWYxQ0h7", "00uy33598308PeFB60h7", "00uy33503zK6oFY8i0h7", "00uy33531qaLfC4La0h7", "00uy33594hZ1ofaI50h7", "00uy334ynwl1lsEu50h7", "00uy335c9oU3h5fbc0h7", "00uy3354dqJTZclCD0h7", "00uy335810xmpj14X0h7", "00uy335d1vWbvs1zM0h7", "00uy335bivPV8AE4C0h7", "00uy3354kyb61rd2Q0h7", "00uy3357n2cLtN0e10h7", "00uy335aj6uo4fkWL0h7", "00uy3350b0emjCLBd0h7", "00uy3357ziHRcz9830h7", "00uy335c4bPr8pKx10h7", "00uy335c77vGvKoUI0h7", "00uy3354vyTCnYd0c0h7", "00uy33501v6u06wCg0h7", "00uy3352snje9LSjY0h7", "00uy335c5xhacwaM90h7", "00uy3350djPceRepj0h7", "00uy3354q8IQDwco60h7", "00uy3351nyo4sW7W60h7", "00uy334zznIoBIhiA0h7", "00uy335axdJgvsmQJ0h7", "00uy335eiqdhiWLPx0h7", "00uy3352vpjd7U91d0h7", "00uy334yv5RxYW19r0h7", "00uy335deyullFpSJ0h7", "00uy3357jePgJnYar0h7", "00uy335cd2nGKfZzP0h7", "00uy3352wmI2LVPfi0h7", "00uy335ek70iq6cdB0h7", "00uy3357tsDZ6IUKh0h7", "00uy3351x63k3IXHd0h7", "00uy335cz2dbtTQAF0h7", "00uy3353ao6X3DWX00h7", "00uy3357ekTswfB7p0h7", "00uy3352qfDH4TLrI0h7", "00uy335c8fmhaJtPo0h7", "00uy335cpe9DjhJnV0h7", "00uy33507nQn8F2fj0h7", "00uy335anr2rmydqK0h7", "00uy334zqrEGj55mN0h7", "00uy3354va0ApgjP10h7", "00uy335d8fJc35yae0h7", "00uy3357y02TWTDJ90h7", "00uy3351uvvPB1Gqy0h7", "00uy335bx0Mxm4s8v0h7", "00uy334ylbSMiaD2u0h7", "00uy3351cakrQ0gJL0h7", "00uy335de7q7PKvy70h7", "00uy334zwqd71GrC20h7", "00uy3359gsKZ5Trz80h7", "00uy335ausoQaoGfQ0h7", "00uy3354hthOsreAw0h7", "00uy33593k6uAcSxm0h7", "00uy3359hjXCcV83W0h7", "00uy334zs6erJEu3Y0h7", "00uy3350bqZf3Rgww0h7", "00uy3354t6wihDARa0h7", "00uy334ypsbzCXf6W0h7", "00uy335d0lWMSEY7G0h7", "00uy335c1rfyrOc1q0h7", "00uy3359n0mtXGqx80h7", "00uy3350a2pwuaT840h7", "00uy3351otvZY3yLH0h7", "00uy335bd8fL3Jd2z0h7", "00uy3352u4rjnimoE0h7", "00uy3353emTzfhdzL0h7", "00uy3357nuxEv4xvO0h7", "00uy3351qdGm8Tl6I0h7", "00uy334yckW23VIdP0h7", "00uy3359kwQXOI4vh0h7", "00uy335ecnW0TKdeM0h7", "00uy335924VFEvgil0h7", "00uy33506vgW087QD0h7", "00uy334zrhKWgSUCd0h7", "00uy335crwj3eGGH50h7", "00uy33549xZ8EcAji0h7", "00uy33518duxmVLbm0h7", "00uy335ee4QFx0dc80h7", "00uy3354mhfYTKldQ0h7", "00uy335cv4rHeZsUq0h7", "00uy3358wtMmRn8je0h7", "00uy335czuQdfwKyW0h7", "00uy3352y14Ysf2iZ0h7", "00uy334ysawauVbQo0h7", "00uy334yfu9aHhybF0h7", "00uy335bphila0RJt0h7", "00uy3354qyprIxvOt0h7", "00uy335dhxFFa41ta0h7", "00uy3359g3s4aVSSa0h7", "00uy334zulLFqnL9v0h7", "00uy335bkjhLHNHpb0h7", "00uy33530zhIpbyJI0h7", "00uy3351gcQF6SuG00h7", "00uy3354npEyuiEDm0h7", "00uy3351wi9oTxECl0h7", "00uy3350651h5E5lE0h7", "00uy335egeZrxkwFb0h7", "00uy335eewKMadXZs0h7", "00uy3357fa5cLh3yU0h7", "00uy3357huUcnxzsB0h7", "00uy334ytz2BLlcvl0h7", "00uy335ekxqFlic0P0h7", "00uy335dfgD7Z9sxc0h7", "00uy335b10IoLTu8z0h7", "00uy33504oC9Jxn960h7", "00uy3351droKmsY3M0h7", "00uy3353dwaLCN9UZ0h7", "00uy3354lprttfCsz0h7", "00uy3351d0uqINIrb0h7", "00uy3358ydjePsFe70h7", "00uy335azl1HR3YOK0h7", "00uy335cfq8QRF0NK0h7", "00uy3357q2irY2Qav0h7", "00uy33592tOftJOIW0h7", "00uy3358z6uYtq0rQ0h7", "00uy33502vF5ghdrm0h7", "00uy33535zwTUSG4a0h7", "00uy335bs37siEiJ80h7", "00uy3354ehChtQEh60h7", "00uy33581gJQMU4XU0h7", "00uy334zozNKAhQnN0h7", "00uy33537jPx3k2NX0h7", "00uy335djeg7iLC9b0h7", "00uy3351shfaKRxsU0h7", "00uy33583ldppMuIT0h7", "00uy335d15kJp9jod0h7", "00uy335d5jOLvxlVE0h7", "00uy3351t9a5sgITz0h7", "00uy335aoz8KUWRjG0h7", "00uy335ebwhTZLlGi0h7", "00uy3354x4sMkzfES0h7", "00uy3357io9pOqnWu0h7", "00uy335b73VLgxOcz0h7", "00uy33519bycEn5Dy0h7", "00uy33536q004xpNO0h7", "00uy335b9uRzjBPXH0h7", "00uy33584d6dleOEY0h7", "00uy335b29fy7poQA0h7", "00uy335b5sGGHalM30h7", "00uy3357w0zaVRV1l0h7", "00uy335cgtXMo8gmH0h7", "00uy3357olqHBAZCC0h7", "00uy335behvqEV4h80h7", "00uy335cydoO1IJqf0h7", "00uy334yjg9921xbS0h7", "00uy3351kz3AVPIoF0h7", "00uy335akcSCaw4Ez0h7", "00uy3357scEBtL5AD0h7", "00uy3359nwqHYp1GL0h7", "00uy334zq1e98WcW40h7", "00uy3357qucMq3Whd0h7", "00uy335cudTzIVjt70h7", "00uy3359a8pZFqcuy0h7", "00uy334zy9YrnbRFK0h7", "00uy3359duPoqwxSQ0h7", "00uy3357dtgHTCEgW0h7", "00uy335bnvAVetwod0h7", "00uy3354idVyAljqc0h7", "00uy3354rpbHNwBwx0h7", "00uy3354f8tGFFufn0h7", "00uy335bt5p58i6640h7", "00uy335013GcMQ0Y00h7", "00uy3351xx4ADGNE90h7", "00uy335dgc2Nn6iSH0h7", "00uy335b4kIDfH5nf0h7", "00uy3354ykWmtx9rr0h7", "00uy335bueGaupupe0h7", "00uy335aq7NowZxL50h7", "00uy33532tU0moV2O0h7", "00uy335c30voCXchj0h7", "00uy3351av8lTQnwW0h7", "00uy335co3JIxcJiv0h7", "00uy3350cxBXEzQgX0h7", "00uy33590nI2FII3t0h7", "00uy3354phDHbpetK0h7", "00uy3354caGVBR3EI0h7", "00uy335aw3TeadJ840h7", "00uy335ckkm2GiLiz0h7", "00uy335d3eIpODnk90h7", "00uy3351htLl0MF3H0h7", "00uy3354h4xENjEmv0h7", "00uy3354j4kSE58Vy0h7", "00uy335csv6UQXEpf0h7", "00uy3351mlRDRe3Kc0h7", "00uy335edfPyMlkye0h7", "00uy3353fdq5YlZ160h7", "00uy334yrgeKNi0sO0h7", "00uy3354zz32OHjt60h7", "00uy3351u2OlAfqNH0h7", "00uy3352tdJHi7PJO0h7", "00uy335ddhpYeDAWE0h7", "00uy3351bkNvOF4OM0h7", "00uy3359jtQSXKWlY0h7", "00uy33582464B1RjK0h7", "00uy3354fxRrf4Snw0h7", "00uy33598qRr59cXs0h7", "00uy3354atX6YbJkG0h7", "00uy335amgZ59BLPQ0h7", "00uy3359lz1zwCSh10h7", "00uy33516sZSGQoH00h7", "00uy3358zxep7XMXH0h7", "00uy335d973GKpOZr0h7", "00uy33500euPQasgr0h7", "00uy3357t1yKomyWv0h7", "00uy335efmIih2EBs0h7"};
        List<String> profileIds = Arrays.asList(arr);
        long start = System.currentTimeMillis();
        Collection<UserProfile> userProfiles = userProfileService.listByProfileIds(profileIds);
        long end = System.currentTimeMillis();
        System.out.println("duration for 320 = " + (end - start));
        assertEquals (arr.length, userProfiles.size(), "wrong number of profiles retrieved");
    }
}
