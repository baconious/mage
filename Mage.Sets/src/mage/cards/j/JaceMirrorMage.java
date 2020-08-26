package mage.cards.j;

import java.util.UUID;

import mage.abilities.Ability;
import mage.abilities.LoyaltyAbility;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.PlaneswalkerEntersWithLoyaltyCountersAbility;
import mage.abilities.condition.common.KickedCondition;
import mage.abilities.decorator.ConditionalInterveningIfTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenCopyTargetEffect;
import mage.abilities.effects.keyword.ScryEffect;
import mage.cards.Card;
import mage.cards.CardsImpl;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.keyword.KickerAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.counters.CounterType;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;

/**
 * @author TheElk801
 */
public final class JaceMirrorMage extends CardImpl {

    public JaceMirrorMage(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.PLANESWALKER}, "{1}{U}{U}");

        this.addSuperType(SuperType.LEGENDARY);
        this.subtype.add(SubType.JACE);
        this.addAbility(new PlaneswalkerEntersWithLoyaltyCountersAbility(4));

        // Kicker {2}
        this.addAbility(new KickerAbility(new ManaCostsImpl<>("{2}")));

        // When Jace, Mirror Mage enters the battlefield, if Jace was kicked, create a token that's a copy of Jace, Mirror Mage except it's not legendary and its starting loyalty is 1.
        this.addAbility(new ConditionalInterveningIfTriggeredAbility(
                new EntersBattlefieldTriggeredAbility(new JaceMirrorMageCopyEffect()),
                KickedCondition.instance, "When {this} enters the battlefield, if {this} was kicked, " +
                "create a token that's a copy of {this} except it's not legendary and its starting loyalty is 1."
        ));

        // +1: Scry 2.
        this.addAbility(new LoyaltyAbility(new ScryEffect(2), 1));

        // 0: Draw a card and reveal it. Remove a number of loyalty counters equal to that card's converted mana cost from Jace, Mirror Mage.
        this.addAbility(new LoyaltyAbility(new JaceMirrorMageDrawEffect(), 0));
    }

    private JaceMirrorMage(final JaceMirrorMage card) {
        super(card);
    }

    @Override
    public JaceMirrorMage copy() {
        return new JaceMirrorMage(this);
    }
}

class JaceMirrorMageCopyEffect extends OneShotEffect {

    JaceMirrorMageCopyEffect() {
        super(Outcome.Benefit);
    }

    private JaceMirrorMageCopyEffect(final JaceMirrorMageCopyEffect effect) {
        super(effect);
    }

    @Override
    public JaceMirrorMageCopyEffect copy() {
        return new JaceMirrorMageCopyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanentOrLKIBattlefield(source.getSourceId());
        if (permanent == null) {
            return false;
        }
        CreateTokenCopyTargetEffect effect = new CreateTokenCopyTargetEffect(source.getControllerId(), null, false, 1);
        effect.setTargetPointer(new FixedTarget(source.getSourceId(), game));
        effect.setIsntLegendary(true);
        effect.setStartingLoyalty(1);
        return effect.apply(game, source);
    }
}

class JaceMirrorMageDrawEffect extends OneShotEffect {

    JaceMirrorMageDrawEffect() {
        super(Outcome.Benefit);
        staticText = "draw a card and reveal it. Remove a number of loyalty counters equal to that card's converted mana cost from {this}";
    }

    private JaceMirrorMageDrawEffect(final JaceMirrorMageDrawEffect effect) {
        super(effect);
    }

    @Override
    public JaceMirrorMageDrawEffect copy() {
        return new JaceMirrorMageDrawEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        // TODO: Make this and similar effects work with draw replacement correctly
        Player player = game.getPlayer(source.getControllerId());
        if (player == null) {
            return false;
        }
        Card card = player.getLibrary().getFromTop(game);
        player.drawCards(1, source.getSourceId(), game);
        player.revealCards(source, new CardsImpl(card), game);
        if (card == null || card.getConvertedManaCost() == 0) {
            return true;
        }
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent == null) {
            return true;
        }
        permanent.removeCounters(CounterType.LOYALTY.createInstance(card.getConvertedManaCost()), game);
        return true;
    }
}
