package mod.ckenja.cyninja.infrastructure.registry;

import mod.ckenja.cyninja.Cyninja;
import mod.ckenja.cyninja.core.action.ActionAttachment;
import mod.ckenja.cyninja.core.action.CooldownAttachment;
import mod.ckenja.cyninja.infrastructure.attachment.*;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Cyninja.MODID);

    public static final Supplier<AttachmentType<ActionAttachment>> ACTION = ATTACHMENT_TYPES.register(
            "ninja_action", () -> AttachmentType.serializable(ActionAttachment::new).build());
    public static final Supplier<AttachmentType<InputAttachment>> INPUT = ATTACHMENT_TYPES.register(
            "ninja_input", () -> AttachmentType.serializable(InputAttachment::new).build());
    public static final Supplier<AttachmentType<ActionStatesAttachment>> STATES = ATTACHMENT_TYPES.register(
            "action_states", () -> AttachmentType.serializable(ActionStatesAttachment::new).build());
    public static final Supplier<AttachmentType<CooldownAttachment>> COOLDOWN = ATTACHMENT_TYPES.register(
            "cooldown", () -> AttachmentType.serializable(CooldownAttachment::new).build());
}